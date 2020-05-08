 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package graphview.layouts;
 
 import geometry.Rect;
 import geometry.Vec2;
 import graphview.GraphNode;
 import graphview.GraphScene;
 import graphview.GraphUtils;
 import java.util.ArrayList;
 
 /**
  *
  * @author Kirill
  */
 public class HierarchicalLayoutK extends BaseLayout{
 
     public static final int ELEM_OFFSET = 10;
     public static final int COLOMN_OFFSET = 10;
     
     @Override
     public void onApplyLayout() {
         ArrayList<ArrayList<GraphNode>> clusters=GraphUtils.findClusters(scene,null);
         
         for(int i=0;i<clusters.size();i++)
         {
             System.out.println(String.format("\nCluster %d:",i));
             calculateCluster(clusters.get(i));
         }   
         
     };
     
     private class TreeElem
     {
         TreeElem(TreeElem parent,Vec2 size, int nRow,int nCol,ArrayList<Integer> prevColInRow, int index_)
         {
             this.size=size;
             this.parent=parent;
             currentRow=nRow;
             currentCol=nCol;
             index=index_;
             colomnInRow.addAll(prevColInRow);
         };
         
         ArrayList<Integer> getCurrColArray()
         {
             ArrayList<Integer> arr=new ArrayList<Integer>();
             for(int i=0;i<colomnInRow.size();i++)
             {
                 arr.add(new Integer(colomnInRow.get(i)));
             };
             arr.add(currentCol);
             return arr;
         };
         
         public void setPrevColInRowArr(ArrayList<Integer> arr)
         {
             colomnInRow.clear();
             colomnInRow.addAll(arr);
             for(int i=0;i<childs.size();i++)
             {
                 childs.get(i).setPrevColInRowArr(getCurrColArray());
             };
         };
         
         public void updateChilds()
         {
             for(int i=0;i<childs.size();i++)
             {
                 childs.get(i).setPrevColInRowArr(getCurrColArray());
             };
         };
         
         public TreeElem addChild(int index,Vec2 sz)
         {
             TreeElem el=new TreeElem(this,sz, currentRow+1,childs.size(),getCurrColArray(),index);
             childs.add(el);
             return el;
         };
         
         public void setCol(int nCol)
         {
             currentCol=nCol;
             updateChilds();
         };
         
         public void swapChilds(int i1, int i2)
         {
             TreeElem tmp=childs.get(i1);
             childs.set(i1, childs.get(i2));
             childs.set(i2, tmp);
             
             childs.get(i1).setCol(i1);
             childs.get(i2).setCol(i2);
         };
         
         public void calculate()
         {
            
            //if(childs.size()<=1) return;
            if(links.size()==0) 
            {
                for(int i=0;i<childs.size();i++)
                    childs.get(i).calculate();
                return;
            }
             
             for(int i=0;i<childs.size();i++)
             {
                 if(childs.get(i).links.size()==0)
                     continue;
                 
                int maxDiff=getSumLinksDist();
                 int maxI=i;
                 for(int j=0;j<childs.size();j++)
                 {
                     if(i==j) continue;
                     swapChilds(i, j);
                    int diff=getSumLinksDist();
                     if(diff<maxDiff)
                     {
                         maxI=j;
                         maxDiff=diff;
                     };
                     swapChilds(i, j);
                 };
                 swapChilds(i, maxI);
             };
             
             for(int i=0;i<childs.size();i++)
                     childs.get(i).calculate();
         };
         
         int getSumLinksDist()
         {
             int d=0;
             for(int i=0;i<links.size();i++)
             {
                 d+=getColDistance(links.get(i));
             };
             return d;
         };
         
         int getColDistance(TreeElem el)
         {
             return Math.abs(currentCol-el.getCurrColArray().get(currentRow));
         };
         
         public TreeElem findIndex(int ind)
         {
             if(ind==index) return this;
             for(int i=0;i<childs.size();i++)
             {
                 TreeElem ret=childs.get(i).findIndex(ind);
                 if(ret!=null) return ret;
             };
             return null;
         };
         
         public boolean isExists(int ind)
         {
             return findIndex(ind) != null;
         };
         
         public void calcRowSizes(float[] rowSizes)
         {
             if(rowSizes[currentRow]<size.y)
             {
                 rowSizes[currentRow]=size.y;
             };
             for(int i=0;i<childs.size();i++)
                 childs.get(i).calcRowSizes(rowSizes);
         };
         
         public void placeNodes(float[] rowSizes, ArrayList<GraphNode> cluster)
         {
             if(childs.size()==0) return;
             Rect currRect=cluster.get(index).getAspect().getGlobalRectangle();
             float topStart=currRect.top+rowSizes[currentRow];
             float leftStart=currRect.getCenter().x+colSize.x;
             float firstStart=leftStart;
             float placeSumm=0;
             float needSumm=colSize.y-colSize.x;
             Vec2 childPos=new Vec2();
             
             System.out.println(String.format("Need summ: %.2f",needSumm));
             for(int i=0;i<childs.size();i++)
             {
                 placeSumm+=childs.get(i).colSize.y-childs.get(i).colSize.x;
                 childPos.set(leftStart+Math.abs(childs.get(i).colSize.x),topStart);
                 childPos.x-=childs.get(i).size.x/2;
                 childPos.x+=ELEM_OFFSET/2;
                 System.out.println(String.format("Node %d: placed ad %.2f [%.2f]", i,childPos.x-firstStart,placeSumm));
                 cluster.get(childs.get(i).index).getAspect().setPosition(childPos);
                 leftStart+=childs.get(i).colSize.y-childs.get(i).colSize.x;
             };
             
             for(int i=0;i<childs.size();i++)
             {
                 childs.get(i).placeNodes(rowSizes,cluster);
             };
         };
         
         public void calcColSize()
         {
             float w=0;
             float c=0;
             colSize.set(0,0);
             
             if(currentRow==0)
             {
                 int t=0;
                 t++;
             };
             
             for(int i=0;i<childs.size();i++)
             {
                 childs.get(i).calcColSize();
                 
                 float sze=(childs.get(i).colSize.y-childs.get(i).colSize.x);
                 if(i==childs.size()/2)
                 {
                     if(childs.size()%2==0)
                     {
                         c=w;
                     }
                     else
                     {
                         c=w+sze/2;
                     };
                 }
                 w+=sze;
             };
             
             colSize.x=-(c);
             colSize.y=(w-c);
             if(Math.abs(colSize.x)<size.x/2)
                 colSize.x=-size.x/2;
             if(colSize.y<size.x/2)
                 colSize.y=size.x/2;
             colSize.x-=ELEM_OFFSET/2;
             colSize.y+=ELEM_OFFSET/2;
             System.out.println(String.format("Col [%d,%d]: l = %.2f, r = %.2f  [%.2f]",currentRow,currentCol,colSize.x,colSize.y,colSize.y-colSize.x));
         };
         
         public int currentRow=-1;
         public int currentCol=-1;
         public ArrayList<Integer> colomnInRow=new ArrayList<Integer>();
         public ArrayList<TreeElem> links=new ArrayList<TreeElem>();
         public ArrayList<TreeElem> childs=new ArrayList<TreeElem>();
         public int index=-1;
         Vec2 size=new Vec2();
         Vec2 colSize=new Vec2();
         TreeElem parent=null;
     };
     
     public void calculateCluster(ArrayList<GraphNode> cluster)
     {
         ArrayList<ArrayList<Integer>> edges=GraphUtils.getClusterEdges(scene,cluster);
         System.out.println("Edges:");
         for(int i=0;i<cluster.size();i++)
         {
             System.out.print(String.format("%s -> [", cluster.get(i).getAspect().getLabel()));
             for(int j=0;j<edges.get(i).size();j++)
             {
                 System.out.print(String.format(" %s ", cluster.get(edges.get(i).get(j)).getAspect().getLabel()));
             };
             System.out.println("]");
         };
         
         ArrayList<Integer> ranks=calculateRanks(cluster,edges,0);
         
         System.out.println("\nRanks:");
         for(int i=0;i<cluster.size();i++)
         {
             System.out.println(String.format("%s:\t%d", cluster.get(i).getAspect().getLabel(),ranks.get(i)));
         };
         
         int rowCount=0;
         int rootIndex=-1;
         for(int i=0;i<ranks.size();i++)
         {
             rowCount=Math.max(rowCount, ranks.get(i));
             if(ranks.get(i)==0) rootIndex=i;
         };
         rowCount++;
         
         TreeElem root=new TreeElem(null,cluster.get(rootIndex).getAspect().getSize(),0,0,new ArrayList<Integer>(),rootIndex);
         reqCalcTree(root,root,cluster,edges,ranks);
         root.calculate();
         
         
         float[] rowSizes=new float[rowCount];
         for(int i=0;i<rowSizes.length;i++)
             rowSizes[i]=0;
         root.calcRowSizes(rowSizes);
         for(int i=0;i<rowSizes.length;i++)
             rowSizes[i]*=3;
         
         root.calcColSize();
         root.placeNodes(rowSizes, cluster);
     };
     
     public void reqCalcTree(TreeElem root, TreeElem curr, ArrayList<GraphNode> cluster,ArrayList<ArrayList<Integer>> edges, ArrayList<Integer> ranks)
     {
         int currIndex=curr.index;
         
         for(int i=0;i<edges.get(currIndex).size();i++)
         {
             int nextIndex=edges.get(currIndex).get(i);
             
             TreeElem ell=root.findIndex(nextIndex);
             if((ranks.get(nextIndex)>=curr.currentRow) && (ell!=null))
             {
                 curr.links.add(ell);
             }
             else if(ranks.get(nextIndex)==curr.currentRow+1)
             {
                 TreeElem el=curr.addChild(nextIndex,cluster.get(nextIndex).getAspect().getSize());
                 reqCalcTree(root,el,cluster,edges,ranks);
             };
         };
     };
     
     public ArrayList<Integer> calculateRanks(ArrayList<GraphNode> cluster,ArrayList<ArrayList<Integer>> edges, int fromIndex)
     {
         ArrayList<Integer> ranks=new ArrayList<Integer>();
         for(int i=0;i<cluster.size();i++) ranks.add(-1);
         
         reqCalcRanks(cluster,edges,ranks,fromIndex,-1);
         
         return ranks;
     };
     
     public void reqCalcRanks(ArrayList<GraphNode> cluster,ArrayList<ArrayList<Integer>> edges, ArrayList<Integer> ranks, int currIndex, int prevRank)
     {
         String curr=cluster.get(currIndex).getAspect().getLabel();
         int currRank=prevRank+1;
         
         if((ranks.get(currIndex)<=currRank) && (ranks.get(currIndex)!=-1)) 
             return;
         
         ranks.set(currIndex,currRank);
         
         for(int i=0;i<edges.get(currIndex).size();i++)
         {
             if(edges.get(currIndex).get(i)==currIndex) continue;
             reqCalcRanks(cluster,edges,ranks,edges.get(currIndex).get(i),currRank);
         };
     };
 }
