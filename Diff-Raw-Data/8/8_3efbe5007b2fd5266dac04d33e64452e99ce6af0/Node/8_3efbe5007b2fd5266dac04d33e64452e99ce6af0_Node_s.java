 package org.chasen.mecab.wrapper;
 
 import org.chasen.mecab.mecab.mecab_node_t;
 import org.chasen.mecab.mecab.mecab_path_t;
 import org.chasen.mecab.wrapper.type.NodeType;
 
 public class Node {
     
     protected final mecab_node_t node;
     
     protected Node(mecab_node_t node){
         this.node = node;
     }
     
     public Node getPrev(){
         mecab_node_t.ByReference prev = node.prev();
         if(null == prev){
             return null;
         }
         return new Node(prev);
     }
     
     public Node getNext(){
         mecab_node_t.ByReference next = node.next();
         if(null == next){
             return null;
         }
         return new Node(next);
     }
     
     public Node getENext(){
         mecab_node_t.ByReference enext = node.enext();
         if(null == enext){
             return null;
         }
         return new Node(enext);
     }
     
     public Node getBNext(){
         mecab_node_t.ByReference bnext = node.bnext();
         if(null == bnext){
             return null;
         }
         return new Node(bnext);
     }
     
     public Path getRpath(){
         mecab_path_t.ByReference rpath = node.rpath();
         if(null == rpath){
             return null;
         }
         return new Path(rpath);
     }
     
     public Path getLPath(){
         mecab_path_t.ByReference lpath = node.lpath();
         if(null == lpath){
             return null;
         }
         return new Path(lpath);
     }
     
     public String getSurface(){
        return node.surface();
     }
     
     public String getFeature(){
         return node.feature();
     }
     
     public int getId(){
         return node.id();
     }
     
     public short getLength(){
         return node.length();
     }
 
     public short getRLength() {
         return node.rlength();
     }
     
     public short getRcAttr() {
         return node.rcAttr();
     }
 
     public short getLcAttr() {
         return node.lcAttr();
     }
 
     public short getPosid() {
         return node.posid();
     }
 
     public byte getCharType() {
         return node.char_type();
     }
 
     public NodeType getStat() {
         return NodeType.get(node.stat());
     }
 
     public short getIsBest() {
         return node.isbest();
     }
 
     public int getSentenceLength() {
         return node.sentence_length();
     }
 
     public float getAlpha() {
         return node.alpha();
     }
 
     public float getBeta() {
         return node.beta();
     }
 
     public float getProb() {
         return node.prob();
     }
 
     public short getWCost() {
         return node.wcost();
     }
 
     public long getCost() {
         return node.cost();
     }
 
 }
