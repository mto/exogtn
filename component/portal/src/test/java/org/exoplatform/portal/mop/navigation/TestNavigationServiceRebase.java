/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.exoplatform.portal.mop.navigation;

import org.exoplatform.portal.mop.SiteKey;
import org.gatein.mop.api.workspace.Navigation;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.core.api.MOPService;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TestNavigationServiceRebase extends AbstractTestNavigationService
{

   public void testRebase1() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "rebase1");
      Navigation def = portal.getRootNavigation().addChild("default");
      def.addChild("a");
      def.addChild("d");

      //
      sync(true);

      //
      NavigationContext navigation = service.loadNavigation(SiteKey.portal("rebase1"));
      Node root1 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).node;
      Node a = root1.getChild("a");
      Node d = root1.getChild("d");
      Node b = root1.addChild(1, "b");

      //
      Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).node;
      Node c2 = root2.addChild(1, "c");
      service.saveNode(root2.context);
      sync(true);

      //
      service.rebaseNode(root1.context, null, null);
      assertEquals(4, root1.getNodeCount());
      assertSame(a, root1.getChild(0));
      assertSame(b, root1.getChild(1));
      Node c1 = root1.getChild(2);
      assertEquals("c", c1.getName());
      assertEquals(c2.getId(), c1.getId());
      assertSame(d, root1.getChild(3));

   }

   public void testRebase2() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "rebase2");
      Navigation def = portal.getRootNavigation().addChild("default");
      def.addChild("a");
      def.addChild("b");

      //
      sync(true);

      //
      NavigationContext navigation = service.loadNavigation(SiteKey.portal("rebase2"));
      Node root1 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).node;
      Node a = root1.getChild("a");
      Node b = root1.getChild("b");
      Node c = a.addChild("c");

      //
      Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).node;
      root2.getChild("b").addChild(root2.getChild("a"));
      service.saveNode(root2.context);
      sync(true);

      //
      service.rebaseNode(root1.context, null, null);
      assertEquals(null, root1.getChild("a"));
      assertSame(b, root1.getChild("b"));
      assertEquals(root1, b.getParent());
      assertSame(a, b.getChild("a"));
      assertEquals(b, a.getParent());
      assertSame(c, a.getChild("c"));
      assertEquals(a, c.getParent());
   }

   public void testRebase3() throws Exception
   {
      MOPService mop = mgr.getPOMService();
      Site portal = mop.getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "rebase3");
      Navigation def = portal.getRootNavigation().addChild("default");
      def.addChild("a");
      def.addChild("b");

      //
      sync(true);

      //
      NavigationContext navigation = service.loadNavigation(SiteKey.portal("rebase3"));
      Node root = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).node;
      root.getChild("a").addChild("foo");

      //
      Node root2 = service.loadNode(Node.MODEL, navigation, Scope.ALL, null).node;
      root2.removeChild("a");
      service.saveNode(root2.context);
      sync(true);

      //
      try
      {
         service.rebaseNode(root.context, null, null);
         fail();
      }
      catch (NavigationServiceException e)
      {
         assertEquals(NavigationError.ADD_CONCURRENTLY_REMOVED_PARENT_NODE, e.getError());
      }

   }
}
