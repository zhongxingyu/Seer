 // -------------------------------------------------------------------------
 // Copyright (c) 2000-2010 Ufinity. All Rights Reserved.
 //
 // This software is the confidential and proprietary information of
 // Ufinity
 //
 // Original author:bixiang Ji
 //
 // -------------------------------------------------------------------------
 // UFINITY MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 // THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 // TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 // PARTICULAR PURPOSE, OR NON-INFRINGEMENT. UFINITY SHALL NOT BE
 // LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING,
 // MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 //
 // THIS SOFTWARE IS NOT DESIGNED OR INTENDED FOR USE OR RESALE AS ON-LINE
 // CONTROL EQUIPMENT IN HAZARDOUS ENVIRONMENTS REQUIRING FAIL-SAFE
 // PERFORMANCE, SUCH AS IN THE OPERATION OF NUCLEAR FACILITIES, AIRCRAFT
 // NAVIGATION OR COMMUNICATION SYSTEMS, AIR TRAFFIC CONTROL, DIRECT LIFE
 // SUPPORT MACHINES, OR WEAPONS SYSTEMS, IN WHICH THE FAILURE OF THE
 // SOFTWARE COULD LEAD DIRECTLY TO DEATH, PERSONAL INJURY, OR SEVERE
 // PHYSICAL OR ENVIRONMENTAL DAMAGE ("HIGH RISK ACTIVITIES"). UFINITY
 // SPECIFICALLY DISCLAIMS ANY EXPRESS OR IMPLIED WARRANTY OF FITNESS FOR
 // HIGH RISK ACTIVITIES.
 // -------------------------------------------------------------------------
 package com.ufinity.marchant.ubank.service;
 
 import com.ufinity.marchant.ubank.bean.Folder;
 import com.ufinity.marchant.ubank.exception.UBankException;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.beanutils.BeanUtils;
 
 /**
  * this class is a node of in directory tree
  * 
  * @author bxji
  * @version 2010-8-17
  */
 public class FolderNode {
 
     private Long folderId;
     private Long parentId;
     private String folderName;
     private Date createTime;
     private Date modifyTime;
     private String directory;
     private Boolean share;
     private String folderType;
     private Long userId;
     private List<FolderNode> subNodes;
 
     /**
      * Constructor for FolderNode
      */
     private FolderNode() {
         this.subNodes = new ArrayList<FolderNode>();
     }
 
     /**
      * Generate a directory Tree
      * 
      * @param folders
      *            In accordance with the directory hierarchy of folders sorted
      *            collection
      * @return return a FolderNode
      */
     public static FolderNode generateFolderTree(List<Folder> folders) {
         FolderNode rootNode = null;
         Map<Long, FolderNode> nodes = new HashMap<Long, FolderNode>();
 
         if (folders == null || folders.isEmpty()) {
             return rootNode;
         }
         // if contains top root directory then the root directory is last one
         Folder lastFolder = folders.get(folders.size() - 1);
         if (lastFolder.getParent() == null) {
             rootNode = new FolderNode();
             copyProperties(rootNode, lastFolder);
             addToTree(nodes, rootNode);
 
             for (int i = 0; i < folders.size() - 1; i++) {
                 Folder folder = folders.get(i);
                 FolderNode node = new FolderNode();
                 copyProperties(node, folder);
                 addToTree(nodes, node);
             }
         }
         else { // part of the tree
             for (int i = 0; i < folders.size(); i++) {
                 Folder folder = folders.get(i);
                 FolderNode node = new FolderNode();
                if (i == 0) {
                    rootNode = node;
                }
                 copyProperties(node, folder);
                 addToTree(nodes, node);
             }
         }
         return rootNode;
     }
 
     /**
      * this method copy a part of 'Folder' property to 'FolderNode'
      * 
      * @param node
      *            tree node
      * @param folder
      *            folder object
      */
     private static void copyProperties(FolderNode node, Folder folder) {
         try {
             BeanUtils.copyProperties(node, folder);
         }
         catch (Exception e) {
         }
 
         Folder parentFolder = folder.getParent();
         if (parentFolder != null) {
             node.setParentId(parentFolder.getFolderId());
         }
         node.setUserId(folder.getUser().getUserId());
     }
 
     /**
      * Will be Folder Node added to the directory tree
      * 
      * @param nodes
      *            FolderNode set
      * @param node
      *            a folderNOde
      * @throws UBankException
      *             throw exception when nodes is null
      */
     private static void addToTree(Map<Long, FolderNode> nodes, FolderNode node) {
         if (node == null) {
             return;
         }
         if (node.getParentId() != null) {
             FolderNode parentNode = nodes.get(node.getParentId());
             parentNode.getSubNodes().add(node);
         }
         nodes.put(node.getFolderId(), node);
     }
 
     /**
      * the getter method of folderId
      * 
      * @return the folderId
      */
     public Long getFolderId() {
         return folderId;
     }
 
     /**
      * the setter method of the folderId
      * 
      * @param folderId
      *            the folderId to set
      */
     public void setFolderId(Long folderId) {
         this.folderId = folderId;
     }
 
     /**
      * the getter method of parentId
      * 
      * @return the parentId
      */
     public Long getParentId() {
         return parentId;
     }
 
     /**
      * the setter method of the parentId
      * 
      * @param parentId
      *            the parentId to set
      */
     public void setParentId(Long parentId) {
         this.parentId = parentId;
     }
 
     /**
      * the getter method of folderName
      * 
      * @return the folderName
      */
     public String getFolderName() {
         return folderName;
     }
 
     /**
      * the setter method of the folderName
      * 
      * @param folderName
      *            the folderName to set
      */
     public void setFolderName(String folderName) {
         this.folderName = folderName;
     }
 
     /**
      * the getter method of createTime
      * 
      * @return the createTime
      */
     public Date getCreateTime() {
         return createTime;
     }
 
     /**
      * the setter method of the createTime
      * 
      * @param createTime
      *            the createTime to set
      */
     public void setCreateTime(Date createTime) {
         this.createTime = createTime;
     }
 
     /**
      * the getter method of modifyTime
      * 
      * @return the modifyTime
      */
     public Date getModifyTime() {
         return modifyTime;
     }
 
     /**
      * the setter method of the modifyTime
      * 
      * @param modifyTime
      *            the modifyTime to set
      */
     public void setModifyTime(Date modifyTime) {
         this.modifyTime = modifyTime;
     }
 
     /**
      * the getter method of directory
      * 
      * @return the directory
      */
     public String getDirectory() {
         return directory;
     }
 
     /**
      * the setter method of the directory
      * 
      * @param directory
      *            the directory to set
      */
     public void setDirectory(String directory) {
         this.directory = directory;
     }
 
     /**
      * the getter method of share
      * 
      * @return the share
      */
     public Boolean getShare() {
         return share;
     }
 
     /**
      * the setter method of the share
      * 
      * @param share
      *            the share to set
      */
     public void setShare(Boolean share) {
         this.share = share;
     }
 
     /**
      * the getter method of folderType
      * 
      * @return the folderType
      */
     public String getFolderType() {
         return folderType;
     }
 
     /**
      * the setter method of the folderType
      * 
      * @param folderType
      *            the folderType to set
      */
     public void setFolderType(String folderType) {
         this.folderType = folderType;
     }
 
     /**
      * the getter method of userId
      * 
      * @return the userId
      */
     public Long getUserId() {
         return userId;
     }
 
     /**
      * the setter method of the userId
      * 
      * @param userId
      *            the userId to set
      */
     public void setUserId(Long userId) {
         this.userId = userId;
     }
 
     /**
      * the getter method of subNodes
      * 
      * @return the subNodes
      */
     public List<FolderNode> getSubNodes() {
         return subNodes;
     }
 
     /**
      * the setter method of the subNodes
      * 
      * @param subNodes
      *            the subNodes to set
      */
     public void setSubNodes(List<FolderNode> subNodes) {
         this.subNodes = subNodes;
     }
 
 }
