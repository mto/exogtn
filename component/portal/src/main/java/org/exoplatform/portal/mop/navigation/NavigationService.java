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

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public interface NavigationService
{

   /**
    * Find and returns a navigation. If no such navigation exist, null is returned instead.
    *
    * @param key the navigation key
    * @return the matching navigation
    */
   Navigation getNavigation(SiteKey key);


   <N> N load(NodeModel<N> model, Navigation navigation, Scope scope);

   <N> N load(NodeModel<N> model, N node, Scope scope);


   Node load(Navigation navigation, Scope scope);

   Node load(Node node, Scope scope);

}
