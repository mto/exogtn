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

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class NavigationImpl implements Navigation
{

   /** . */
   private final SiteKey key;

   /** . */
   private final NavigationState state;

   NavigationImpl(SiteKey key)
   {
      this(key, null);
   }

   NavigationImpl(SiteKey key, NavigationState state)
   {
      if (key == null)
      {
         throw new NullPointerException();
      }

      //
      this.key = key;
      this.state = state;
   }

   public SiteKey getKey()
   {
      return key;
   }

   public NavigationState getState()
   {
      return state;
   }
}