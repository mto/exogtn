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

import java.util.Collection;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class DefaultNodeModel implements NodeModel<Node>
{

   static final DefaultNodeModel INSTANCE = new DefaultNodeModel();

   public DefaultNodeModel()
   {
   }

   public String getId(Node node)
   {
      return node.data.getId();
   }

   public Node create(NodeData data, Collection<Node> children)
   {
      Node.Relationships fragment = new Node.Relationships(children.size());
      for (Node node : children)
      {
         fragment.put(node.data.getName(), node);
      }
      return new Node(data, fragment);
   }

   public Node create(NodeData data)
   {
      return new Node(data);
   }
}