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

package org.exoplatform.web.controller.router;

import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.controller.metadata.RouteMetaData;
import org.exoplatform.web.controller.metadata.RouterMetaData;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Router
{

   /** . */
   final Route root;

   public Router(RouterMetaData metaData)
   {
      this.root = new Route();

      //
      for (RouteMetaData routeMetaData : metaData.getRoutes())
      {
         addRoute(routeMetaData);
      }
   }

   public void addRoute(RouteMetaData routeMetaData)
   {
      root.append(routeMetaData.getPath(), routeMetaData.getParameters());
   }

   public String render(Map<QualifiedName, String> parameters)
   {
      return root.render(parameters);
   }

   public Map<QualifiedName, String> process(String path) throws IOException
   {
      return root.route(path, new HashMap<QualifiedName, String>());
   }
}