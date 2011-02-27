/*
 * Copyright (C) 2010 eXo Platform SAS.
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

import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.Visibility;
import org.gatein.mop.api.workspace.Navigation;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * An immutable node data class.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class NodeData implements Node.Data, Serializable
{

   /** . */
   final String id;

   /** . */
   final String name;

   /** . */
   final String uri;

   /** . */
   final String label;

   /** . */
   final String icon;

   /** . */
   final Date startPublicationDate;

   /** . */
   final Date endPublicationDate;

   /** . */
   final Visibility visibility;

   /** . */
   final String pageRef;

   /** . */
   final LinkedHashMap<String, String> children;

   NodeData(Navigation nav)
   {
      LinkedHashMap<String, String> children = new LinkedHashMap<String, String>();
      for (Navigation child : nav.getChildren())
      {
         children.put(child.getName(), child.getObjectId());
      }

      //
      String label = null;
      if (nav.isAdapted(Described.class))
      {
         Described described = nav.adapt(Described.class);
         label = described.getName();
      }

      //
      this.id = nav.getObjectId();
      this.name = nav.getName();
      this.uri = null;
      this.label = label;
      this.icon = null;
      this.startPublicationDate = null;
      this.endPublicationDate = null;
      this.visibility = null;
      this.pageRef = null;
      this.children = children;
   }

   public String getId()
   {
      return id;
   }

   public String getName()
   {
      return name;
   }

   public String getURI()
   {
      return uri;
   }

   public String getLabel()
   {
      return label;
   }

   public String getIcon()
   {
      return icon;
   }

   public Date getStartPublicationDate()
   {
      return startPublicationDate;
   }

   public Date getEndPublicationDate()
   {
      return endPublicationDate;
   }

   public Visibility getVisibility()
   {
      return visibility;
   }

   public String getPageRef()
   {
      return pageRef;
   }
}