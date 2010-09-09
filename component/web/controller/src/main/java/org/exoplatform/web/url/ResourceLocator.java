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

package org.exoplatform.web.url;

import java.io.IOException;

/**
 * <p>A locator for a resource.</p>
 *
 * <p>This class is abstract to allow locator subclass to add specific parameters.</p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 * @param <R> the resource parameter type
 */
public interface ResourceLocator<R>
{

   /**
    * Returns the current resource actually set on this locator.
    *
    * @return the resource
    */
   R getResource();

   /**
    * Set the resource on this locator.
    *
    * @param resource the resource to set
    */
   void setResource(R resource);

   /**
    * Append the resource locator path.
    *
    * @param appendable the appendable
    * @throws IOException any IOException thrown by the appendable
    */
   void append(Appendable appendable) throws IOException;

}