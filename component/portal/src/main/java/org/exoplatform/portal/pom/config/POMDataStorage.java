/**
 * Copyright (C) 2009 eXo Platform SAS.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.portal.pom.config;

import org.chromattic.api.ChromatticSession;
import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.portal.application.PortletPreferences;
import org.exoplatform.portal.config.NoSuchDataException;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.CloneApplicationState;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PersistentApplicationState;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.pom.config.tasks.DashboardTask;
import org.exoplatform.portal.pom.config.tasks.MOPAccess;
import org.exoplatform.portal.pom.config.tasks.PageTask;
import org.exoplatform.portal.pom.config.tasks.PortalConfigTask;
import org.exoplatform.portal.pom.config.tasks.PortletPreferencesTask;
import org.exoplatform.portal.pom.config.tasks.PreferencesTask;
import org.exoplatform.portal.pom.config.tasks.SearchTask;
import org.exoplatform.portal.pom.data.ApplicationData;
import org.exoplatform.portal.pom.data.ComponentData;
import org.exoplatform.portal.pom.data.ContainerData;
import org.exoplatform.portal.pom.data.DashboardData;
import org.exoplatform.portal.pom.data.Mapper;
import org.exoplatform.portal.pom.data.ModelChange;
import org.exoplatform.portal.pom.data.ModelData;
import org.exoplatform.portal.pom.data.ModelDataStorage;
import org.exoplatform.portal.pom.data.OwnerKey;
import org.exoplatform.portal.pom.data.PageData;
import org.exoplatform.portal.pom.data.PageKey;
import org.exoplatform.portal.pom.data.PortalData;
import org.exoplatform.portal.pom.data.PortalKey;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.WorkspaceObject;
import org.gatein.mop.api.workspace.ui.UIComponent;
import org.gatein.mop.api.workspace.ui.UIContainer;
import org.gatein.mop.api.workspace.ui.UIWindow;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.impl.UnmarshallingContext;
import java.io.ByteArrayInputStream;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class POMDataStorage implements ModelDataStorage
{

   /** . */
   private final POMSessionManager pomMgr;

   /** . */
   private ConfigurationManager confManager_;

   public POMDataStorage(POMSessionManager pomMgr, ConfigurationManager confManager)
   {
      this.pomMgr = pomMgr;
      this.confManager_ = confManager;
   }

   public PortalData getPortalConfig(PortalKey key) throws Exception
   {
      return pomMgr.execute(new PortalConfigTask.Load(key));
   }

   public void create(PortalData config) throws Exception
   {
      pomMgr.execute(new PortalConfigTask.Save(config, false));
   }

   public void save(PortalData config) throws Exception
   {
      pomMgr.execute(new PortalConfigTask.Save(config, true));
   }

   public void remove(PortalData config) throws Exception
   {
      pomMgr.execute(new PortalConfigTask.Remove(config.getKey()));
   }

   public PageData getPage(PageKey key) throws Exception
   {
      return pomMgr.execute(new PageTask.Load(key));
   }

   public PageData clonePage(PageKey key, PageKey cloneKey) throws Exception
   {
      return pomMgr.execute(new PageTask.Clone(key, cloneKey, true));
   }

   public void remove(PageData page) throws Exception
   {
      pomMgr.execute(new PageTask.Remove(page));
   }

   public void create(PageData page) throws Exception
   {
      pomMgr.execute(new PageTask.Save(page));
   }

   public List<ModelChange> save(PageData page) throws Exception
   {
      PageTask.Save task = new PageTask.Save(page);
      pomMgr.execute(task);
      return task.getChanges();
   }

   public void save(PortletPreferences portletPreferences) throws Exception
   {
      pomMgr.execute(new PortletPreferencesTask.Save(portletPreferences));
   }

   public <S> String getId(ApplicationState<S> state) throws Exception
   {
      String contentId;
      if (state instanceof TransientApplicationState)
      {
         TransientApplicationState tstate = (TransientApplicationState)state;
         contentId = tstate.getContentId();
      }
      else if (state instanceof PersistentApplicationState)
      {
         PersistentApplicationState pstate = (PersistentApplicationState)state;
         contentId = pomMgr.execute(new PreferencesTask.GetContentId<S>(pstate.getStorageId()));
      }
      else if (state instanceof CloneApplicationState)
      {
         CloneApplicationState cstate = (CloneApplicationState)state;
         contentId = pomMgr.execute(new PreferencesTask.GetContentId<S>(cstate.getStorageId()));
      }
      else
      {
         throw new AssertionError();
      }

      //
      return contentId;
   }

   public <S> S load(ApplicationState<S> state, ApplicationType<S> type) throws Exception
   {
      Class<S> clazz = type.getContentType().getStateClass();
      if (state instanceof TransientApplicationState)
      {
         TransientApplicationState<S> transientState = (TransientApplicationState<S>)state;
         S prefs = transientState.getContentState();
         return prefs != null ? prefs : null;
      }
      else if (state instanceof CloneApplicationState)
      {
         PreferencesTask.Load<S> load = new PreferencesTask.Load<S>(((CloneApplicationState<S>)state).getStorageId(), clazz);
         return pomMgr.execute(load);
      }
      else
      {
         PreferencesTask.Load<S> load = new PreferencesTask.Load<S>(((PersistentApplicationState<S>)state).getStorageId(), clazz);
         return pomMgr.execute(load);
      }
   }

   public <S> ApplicationState<S> save(ApplicationState<S> state, S preferences) throws Exception
   {
      if (state instanceof TransientApplicationState)
      {
         throw new AssertionError("Does not make sense");
      }
      else
      {
         if (state instanceof PersistentApplicationState)
         {
            PreferencesTask.Save<S> save =
               new PreferencesTask.Save<S>(((PersistentApplicationState<S>)state).getStorageId(), preferences);
            pomMgr.execute(save);
         }
         else
         {
            PreferencesTask.Save<S> save =
               new PreferencesTask.Save<S>(((CloneApplicationState<S>)state).getStorageId(), preferences);
            pomMgr.execute(save);
         }
         return state;
      }
   }

   public PortletPreferences getPortletPreferences(String windowID) throws Exception
   {
      return pomMgr.execute(new PortletPreferencesTask.Load(windowID));
   }

   public <T> LazyPageList<T> find(Query<T> q) throws Exception
   {
      return find(q, null);
   }

   public <T> LazyPageList<T> find(Query<T> q, Comparator<T> sortComparator) throws Exception
   {
      Class<T> type = q.getClassType();
      if (PageData.class.equals(type))
      {
         ListAccess<PageData> pageAccess;
         try
         {
            pageAccess = new MOPAccess.PageAccess(pomMgr, (Query<PageData>)q);
         }
         catch (IllegalArgumentException e)
         {
            pageAccess = new ListAccess<PageData>()
            {
               @Override
               public PageData[] load(int index, int length) throws Exception, IllegalArgumentException
               {
                  return new PageData[0];
               }

               @Override
               public int getSize() throws Exception
               {
                  return 0;
               }
               
            };
         }
         return (LazyPageList<T>)new LazyPageList<PageData>(pageAccess, 10);
      }
      else if (PortletPreferences.class.equals(type))
      {
         return (LazyPageList<T>)pomMgr.execute(new SearchTask.FindPortletPreferences((Query<PortletPreferences>)q));
      }
      else if (PortalData.class.equals(type))
      {
         return (LazyPageList<T>)pomMgr.execute(new SearchTask.FindSite((Query<PortalData>)q));
      }
      else if (PortalKey.class.equals(type) && "portal".equals(q.getOwnerType()))
      {
         return (LazyPageList<T>)pomMgr.execute(new SearchTask.FindSiteKey((Query<PortalKey>)q));
      }
      else if (PortalKey.class.equals(type) && "group".equals(q.getOwnerType()))
      {
         return (LazyPageList<T>)pomMgr.execute(new SearchTask.FindSiteKey((Query<PortalKey>)q));
      }
      else
      {
         throw new UnsupportedOperationException("Could not perform search on query " + q);
      }
   }

   /**
    * This is a hack and should be removed, it is only used temporarily.
    * This is because the objects are loaded from files and don't have name.
    */
   private void generateStorageName(ModelObject obj)
   {
      if (obj instanceof Container)
      {
         for (ModelObject child : ((Container)obj).getChildren())
         {
            generateStorageName(child);
         }
      }
      else if (obj instanceof Application)
      {
         obj.setStorageName(UUID.randomUUID().toString());
      }
   }

   public DashboardData loadDashboard(String dashboardId) throws Exception
   {
      return pomMgr.execute(new DashboardTask.Load(dashboardId));
   }

   public void saveDashboard(DashboardData dashboard) throws Exception
   {
      pomMgr.execute(new DashboardTask.Save(dashboard));
   }

   public Container getSharedLayout() throws Exception
   {
      String path = "war:/conf/portal/portal/sharedlayout.xml";
      String out = IOUtil.getStreamContentAsString(confManager_.getInputStream(path));
      ByteArrayInputStream is = new ByteArrayInputStream(out.getBytes("UTF-8"));
      IBindingFactory bfact = BindingDirectory.getFactory(Container.class);
      UnmarshallingContext uctx = (UnmarshallingContext)bfact.createUnmarshallingContext();
      uctx.setDocument(is, null, "UTF-8", false);
      Container container = (Container)uctx.unmarshalElement();
      generateStorageName(container);
      return container;
   }

   public void save() throws Exception
   {
      pomMgr.execute(new POMTask<Object>()
      {
         public Object run(POMSession session) throws Exception
         {
            session.save();
            return null;
         }
      });
   }
   
   public <A> A adapt(ModelData modelData, Class<A> type)
   {
      return adapt(modelData, type, true);
   }
   
   public <A> A adapt(ModelData modelData, Class<A> type, boolean create)
   {
      try
      {
         POMSession pomSession = pomMgr.getSession();
         ChromatticSession chromSession = pomSession.getSession();
         
         //TODO: Deal with the case where modelData is not persisted before invocation to adapt
         // Get the workspace object
         Object o = pomSession.findObjectById(modelData.getStorageId());
         
         A a = chromSession.getEmbedded(o, type);
         if(a == null && create)
         {
            a = chromSession.create(type);
            chromSession.setEmbedded(o, type, a);
         }
         
         return a;
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
         return null;
      }
   }

   public String[] getSiteInfo(String workspaceObjectId) throws Exception
   {

      POMSession session = pomMgr.getSession();

      WorkspaceObject workspaceObject = session.findObjectById(workspaceObjectId);

      if (workspaceObject instanceof UIComponent)
      {
         Site site = ((UIComponent)workspaceObject).getPage().getSite();
         ObjectType<? extends Site> siteType = site.getObjectType();

         String[] siteInfo = new String[2];

         //Put the siteType on returned map
         if (siteType == ObjectType.PORTAL_SITE)
         {
            siteInfo[0] = PortalConfig.PORTAL_TYPE;
         }
         else if (siteType == ObjectType.GROUP_SITE)
         {
            siteInfo[0] = PortalConfig.GROUP_TYPE;
         }
         else if (siteType == ObjectType.USER_SITE)
         {
            siteInfo[0] = PortalConfig.USER_TYPE;
         }

         //Put the siteOwner on returned map
         siteInfo[1] = site.getName();

         return siteInfo;
      }
	   
	   throw new Exception("The provided ID is not associated with an application");
	}

   public <S> ApplicationData<S> getApplicationData(String applicationStorageId) throws Exception
   {
      // TODO Auto-generated method stub

      POMSession session = pomMgr.getSession();
      WorkspaceObject workspaceObject = session.findObjectById(applicationStorageId);

      if (workspaceObject instanceof UIWindow)
      {
         UIWindow application = (UIWindow)workspaceObject;
         Mapper mapper = new Mapper(session);

         ApplicationData data = mapper.load(application);
         return data;
      }
      throw new NoSuchDataException("Could not load the application data specified by the ID: " + applicationStorageId);
   }

   public ContainerData createContainerData(String parentID, ContainerData container) throws Exception
   {
      if(container.getStorageId() != null)
      {
         throw new IllegalArgumentException("Given container must be transient one");
      }

      POMSession session = pomMgr.getSession();
      UIContainer parent = session.findObjectById(ObjectType.CONTAINER, parentID);

      String storageName = container.getStorageName();
      if(storageName == null)
      {
         storageName = UUID.randomUUID().toString();
      }

      UIContainer workspaceDst = parent.add(ObjectType.CONTAINER, storageName);
      ContainerData tempPersistContainer =
         new ContainerData(
            workspaceDst.getObjectId(),
            container.getId(),
            storageName,
            container.getIcon(),
            container.getTemplate(),
            container.getFactoryId(),
            container.getTitle(),
            container.getDescription(),
            container.getWidth(),
            container.getHeight(),
            container.getAccessPermissions(),
            container.getChildren()
         );

      Mapper mapper = new Mapper(session);
      mapper.save(tempPersistContainer, workspaceDst);
      mapper.saveChildren(tempPersistContainer, workspaceDst);

      clearRelevantCache(session, parent);

      List<ComponentData> children = mapper.loadChildren(workspaceDst);

      return new ContainerData(
         tempPersistContainer.getStorageId(),
         tempPersistContainer.getId(),
         tempPersistContainer.getName(),
         tempPersistContainer.getIcon(),
         tempPersistContainer.getTemplate(),
         tempPersistContainer.getFactoryId(),
         tempPersistContainer.getTitle(),
         tempPersistContainer.getDescription(),
         tempPersistContainer.getWidth(),
         tempPersistContainer.getHeight(),
         tempPersistContainer.getAccessPermissions(),
         children
      );
   }

   public ContainerData saveContainerData(ContainerData container) throws Exception
   {
      String storageID = container.getStorageId();
      if(storageID == null)
      {
         throw new IllegalArgumentException("StorageID of given container is null");
      }

      POMSession session = pomMgr.getSession();
      UIContainer dst = session.findObjectById(ObjectType.CONTAINER, storageID);

      if(dst == null)
      {
         throw new Exception("Associated workspace object not found");
      }

      Mapper mapper = new Mapper(session);
      mapper.save(container, dst);
      mapper.saveChildren(container, dst);

      clearRelevantCache(session, dst);

      List<ComponentData> children = mapper.loadChildren(dst);

      return new ContainerData(
         storageID,
         container.getId(),
         container.getName(),
         container.getIcon(),
         container.getTemplate(),
         container.getFactoryId(),
         container.getTitle(),
         container.getDescription(),
         container.getWidth(),
         container.getHeight(),
         container.getAccessPermissions(),
         children);
   }

   public boolean deleteContainerData(ContainerData container) throws Exception
   {
      String storageID = container.getStorageId();
      if(storageID == null)
      {
         throw new IllegalArgumentException("StorageID of given container is null");
      }

      POMSession session = pomMgr.getSession();
      UIContainer deleteObject = session.findObjectById(ObjectType.CONTAINER, storageID);

      if(deleteObject == null)
      {
         throw new Exception("Associated workspace object not found!");
      }

      UIContainer parent = deleteObject.getParent();
      parent.getComponents().remove(deleteObject);
      clearRelevantCache(session, parent);

      return true;
   }

   private void clearRelevantCache(POMSession session, UIContainer src)
   {
      org.gatein.mop.api.workspace.Page page = src.getPage();
      Site site = page.getSite();

      String siteType = Mapper.getOwnerType(site.getObjectType());
      String siteName = site.getName();

      OwnerKey key = null;

      if(isPageData(page))
      {
         key = new PageKey(siteType, siteName, page.getName());
      }
      else
      {
         key = new PortalKey(siteType, siteName);
      }
      session.scheduleForEviction(key);
   }

   /**
    * Check if given workspace Page object is associated with content of a page
    *
    * @param page
    * @return
    */
   private boolean isPageData(org.gatein.mop.api.workspace.Page page)
   {
      org.gatein.mop.api.workspace.Page tempPage = page;
      while(tempPage != null)
      {
         if("pages".equals(tempPage.getName()))
         {
            return true;
         }
         else if("templates".equals(tempPage.getName()))
         {
            return false;
         }
         else
         {
            org.gatein.mop.api.workspace.Page parent = tempPage.getParent();

            //Avoid infinite loop
            if(parent == tempPage)
            {
               return false;
            }
            else
            {
               tempPage = parent;
            }
         }
      }
      return false;
   }

}
