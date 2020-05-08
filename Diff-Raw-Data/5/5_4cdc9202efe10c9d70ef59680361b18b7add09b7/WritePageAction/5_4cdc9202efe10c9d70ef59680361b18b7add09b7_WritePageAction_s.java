 /*
  * Copyright (C) 2003-2010 eXo Platform SAS.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Affero General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, see<http://www.gnu.org/licenses/>.
  */
 package org.exoplatform.jcr.benchmark.usecases.portal;
 
 import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
 
 import java.util.Random;
 
 import javax.jcr.Node;
 import javax.jcr.NodeIterator;
 import javax.jcr.PropertyType;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 import javax.jcr.Value;
 
 /**
  * Created by The eXo Platform SAS.
  * 
  * <br/>Date: 
  *
  * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a> 
  * @version $Id: WritePageAction.java 111 2008-11-11 11:11:11Z serg $
  */
 public class WritePageAction extends AbstractWriteAction
 {
 
    protected int removeProperties = 0;
 
    protected int setProperties = 0;
 
    protected int removeSubNodes = 0;
 
    protected int addSubNodes = 0;
 
    private Random random;
 
    public WritePageAction(RepositoryImpl repository, String workspace, String rootName, int depth, String stringValue,
       byte[] binaryValue, int multiValueSize, int removeProperties, int setProperties, int removeSubNodes,
       int addSubNodes)
    {
       super(repository, workspace, rootName, depth, stringValue, binaryValue, multiValueSize);
       this.removeProperties = removeProperties;
       this.setProperties = setProperties;
       this.removeSubNodes = removeSubNodes;
       this.addSubNodes = addSubNodes;
 
       random = new Random();
    }
 
    @Override
    public void perform() throws RepositoryException
    {
       Session session = null;
       try
       {
          session = getSession(false);
          Node testRoot = session.getRootNode().getNode(getRootNodeName());
 
          // remove properties
          Node node = nextNode(testRoot);
 
          for (int i = 0; i < removeProperties; i++)
          {
            if (node.hasProperties())
             {
                String propName = null;
                do
                {
                   // randomly get next property name
                   propName = this.nextPropertyName();
                }
               while (node.hasProperty(propName));
 
                node.getProperty(propName).remove();
                node.save();
             }
             else
             {
                // there is no more properties
                break;
             }
          }
 
          //add and update properties 
          for (int i = 0; i < setProperties; i++)
          {
             //TODO make generic method
             int propType = random.nextInt(9) + 1;
             boolean isMultivalued = random.nextBoolean();
             String propName = PropertyType.nameFromValue(propType) + (isMultivalued ? "-m" : "-s");
             Value[] values = this.createValues(node, propType, isMultivalued, session.getValueFactory());
             if (isMultivalued)
             {
                node.setProperty(propName, values, propType);
             }
             else
             {
                node.setProperty(propName, values[0], propType);
             }
             node.save();
          }
 
          Node parentNode = this.nextParent(testRoot);
          //delete nodes
          for (int i = 0; i < this.removeSubNodes; i++)
          {
             NodeIterator iterator = parentNode.getNodes();
             if (iterator.hasNext())
             {
                iterator.skip(random.nextInt((int)iterator.getSize()));
                iterator.nextNode().remove();
                parentNode.save();
             }
             else
             {
                // there is nothing to remove
                break;
             }
          }
 
          //add nodes
          for (int i = 0; i < this.addSubNodes; i++)
          {
             createGenericNode(parentNode, session.getValueFactory());
             parentNode.save();
          }
 
       }
       finally
       {
          if (session != null)
          {
             session.logout();
          }
       }
    }
 }
