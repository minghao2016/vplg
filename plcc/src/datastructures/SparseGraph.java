/*
 * This file is part of the Visualization of Protein Ligand Graphs (VPLG) software package.
 *
 * Copyright Tim Schäfer 2012. VPLG is free software, see the LICENSE and README files for details.
 *
 * @author ts
 */

package datastructures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import graphformats.IGraphMLFormat;
import io.IO;
import graphformats.ITrivialGraphFormat;
import java.util.List;

/**
 * An undirected sparse graph. Uses index based access to vertices and
 * adjacency list representation of the edges.
 * 
 * V is the vertex type and E is the edge info type.
 * 
 * @author ts
 */
public class SparseGraph<V, E> implements SimpleGraphInterface {
    
    
    /** Some vertices. Graphs like them. */
    protected List<V> vertices;
    
    /** The edges, encoded as adjacency lists. */
    protected ArrayList<ArrayList<Integer>> edges;
    protected HashMap<String, E> edgeInfo;
    
    public static final Integer EDGETYPE_NONE = 0;
    public static final Integer EDGETYPE_EDGE = 1;
    
    
    /**
     * Constructs a graph from a vertex list. The list may be empty, of course.
     * @param vertList the vertex list
     */
    public SparseGraph(List<V> vertList) {
        this.vertices = vertList;        
        edges = new ArrayList<ArrayList<Integer>>();
        edgeInfo = new HashMap<String, E>();
        // add one ArrayList for each vertex
        for (Integer i = 0; i < vertices.size(); i++) {
            edges.add(new ArrayList<Integer>());
        }
    }
    
    /**
     * Constructs an empty graph.
     */
    public SparseGraph() {
        this.vertices = new ArrayList<V>();
        edges = new ArrayList<ArrayList<Integer>>();
        edgeInfo = new HashMap<String, E>();
        // add one ArrayList for each vertex
        for (Integer i = 0; i < vertices.size(); i++) {
            edges.add(new ArrayList<Integer>());
        }
    }
    
    /**
     * Returns the edge name string for the edge info HashMap. Used internally only.
     * @param i the vertex neighbourIndex by index
     * @param j the vertex j by index
     * @return the edge name
     */
    private String getEdgeName(int i, int j) {
        return "" + i + "#" + j + "";
    }
    
    
    /**
     * Adds an edge between the vertices at indices neighbourIndex and j.
     * @param i the vertex neighbourIndex by index
     * @param j the vertex j by index 
     * @param e the edge info
     */ 
    public void addEdge(int i, int j, E e) {
        if(! this.edges.get(i).contains(j)) {
          this.edges.get(i).add(j);
          this.setEdgeInfo(i, j, e);
        }
        if(! this.edges.get(j).contains(i)) {
          this.edges.get(j).add(i);
          this.setEdgeInfo(j, i, e);
        }
    }
    
    
    /**
     * Returns the index of the vertex object v in this graph, or a value smaller
     * than 0 if this graph contains no such vertex.
     * @param v the Vertex
     * @return the index if the vertex is found, a value smaller than zero otherwise
     */
    public int getVertexIndex(V v) {
        int idx = -1;
        for(int i = 0; i < this.vertices.size(); i++) {
            if(this.vertices.get(i).equals(v)) {
                idx = i;
                return idx;
            }
        }
        return idx;
    }
    
    public void addVertex(V v) {
        this.vertices.add(v);
        this.edges.add(new ArrayList<Integer>());
    }
    
    public Boolean deleteVertex(int idx) {
        
        if( ! this.hasVertexWithIndex(idx)) {
            return false;
        }
        
        // delete edges first
        int numNeighbors = this.neighborsOf(idx).size();
        for(int i = (numNeighbors - 1); i >= 0; i--) {
            deleteEdge(idx, i);            
        }
        
        // ... now the vert
        this.vertices.remove(idx);
        
        // now remove the adjacency list of the vertex
        this.edges.remove(idx);
        
        // now fix all edges (and the edgeInfo objects) which point to wrong (changed) vertex indices. These are the edges which involve at least one vertex with index >= the deleted one.
        for(int i = 0; i < this.edges.size(); i++) {
            List<Integer> adjList = this.edges.get(i);
            for(int j = 0; j < adjList.size(); j++) {
               int neighborIndex = adjList.get(j);
               
               
               if(neighborIndex >= idx) {
                   // fix the edge
                   adjList.set(j, (neighborIndex - 1));
                   
                   // fix the edge info
                    E e = this.getEdgeInfo(i, neighborIndex);
                    this.deleteEdgeInfo(i, neighborIndex);
                    this.setEdgeInfo(i, neighborIndex-1, e);
               }                              
            }
        }
        
        return true;
    }
    
    /**
     * Deletes an edge by index, also taking care of the edge info.
     * @param i first index
     * @param j 2nd index
     * @return true if the edge existed and was removed, false otherwise
     */
    public Boolean deleteEdge(int i, int j) {
        if( ! this.hasEdge(i, j)) {
            return false;
        }
        
        if(this.edges.get(i).contains(j)) {
            //System.out.println("deleteEdge("+i+","+j+"): edge list for "+i+": " + IO.intListToString(this.edges.get(i)));
            int idxj = this.edges.get(i).indexOf(j);
            Integer removedObject = this.edges.get(i).remove(idxj);            
        }
        
        if(this.edges.get(j).contains(i)) {
            //System.out.println("deleteEdge("+i+","+j+"): edge list for "+j+": " + IO.intListToString(this.edges.get(j)));
            int idxi = this.edges.get(j).indexOf(i);
            Integer removedObject = this.edges.get(j).remove(idxi);
        }
        
        deleteEdgeInfo(i, j);
        return true;
    }
    
    /**
     * Checks whether an edge exists between the vertices at index neighbourIndex and j.
     * @param i the vertex neighbourIndex by index
     * @param j the vertex j by index
     * @return true if neighbourIndex and j are adjacent, false otherwise
     */ 
    public boolean hasEdge(int i, int j) {
        return this.edges.get(i).contains(j);
    }
    
    public boolean hasVertexWithIndex(int i) {
        if(this.getNumVertices() <= 0) {
            return false;
        }
        
        if(i >= 0 && i < this.getNumVertices()) {
            return true;
        }
        return false;
    }
    
    /**
     * Returns the EdgeInfo for the edge between vertices at indices neighbourIndex and j
     * @param i the vertex neighbourIndex by index
     * @param j the vertex j by index
     * @return the EdgeInfo for the edge between vertices at indices neighbourIndex and j
     */
    public E getEdgeInfo(int i, int j) {
        return this.edgeInfo.get(this.getEdgeName(i, j));
    }
    
    private void deleteEdgeInfo(int i, int j) {
        this.edgeInfo.remove(this.getEdgeName(i, j));
    }
    
    
    /**
     * Sets the EdgeInfo for the edge between vertices at indices neighbourIndex and j. Note that the edge has to exist already.
     * @param i the vertex neighbourIndex by index
     * @param j the vertex j by index
     * @param e the EdgeInfo
     * @return true if such and edge exists and the data was set, false otherwise
     */
    public boolean setEdgeInfo(int i, int j, E e) {
        if(this.hasEdge(i, j)) {
            edgeInfo.put(this.getEdgeName(i, j), e);
            return true;
        }
        return false;
    }
    
    /**
     * Returns the total number of vertices in this graph.
     * @return the total vertex count
     */
    public int getNumVertices() {
        return this.vertices.size();
    }
    
    
    /**
     * Returns the total number of edges in this graph.
     * @return the total edge count
     */
    public int getNumEdges() {
        int numTotal = 0;
        for(int i = 0; i < this.edges.size(); i++) {
            numTotal += this.edges.get(i).size();
        }
        return numTotal;    
    }
    
    
    /**
     * Returns the degree of vertex at index neighbourIndex.
     * @param vIndex the vertex index
     * @return the vertex degree, neighbourIndex.adjList., the number of vertices adjacent to neighbourIndex
     */
    public int getVertexDegree(int vIndex) {
        return this.edges.get(vIndex).size();
    }
    
    
    /**
     * Returns a list of edges in this graph. Each integer array (of length 2) in the returned list holds
     * the indices of a pair of adjacent vertices. (Do not try to modify the list to modify the edges of the graph, it is a copy.)
     * @return a list of vertex pairs given by their indices which are neighbors
     */
    public ArrayList<Integer[]> getEdgeListIndex() {
        ArrayList<Integer[]> allEdges = new ArrayList<Integer[]>();
        for(int i = 0; i < this.edges.size(); i++) {
            for(int j = 0; j < this.edges.get(i).size(); j++) {
               int neighborOfI = this.edges.get(i).get(j);
               allEdges.add(new Integer[]{i, neighborOfI});
            }
        }
        return allEdges;
    }
    
    @Override
    public List<Integer> neighborsOf(Integer vertIndex) {
        List<Integer> neighbors = new ArrayList<>();
        for(int j = 0; j < this.edges.get(vertIndex).size(); j++) {
            neighbors.add(this.edges.get(vertIndex).get(j));
         }
        return neighbors;
    }
    
    /**
     * Returns the vertex at index idx
     * @param idx the vertex index
     * @return the vertex at index idx
     */
    public V getVertex(int idx) {
        return this.vertices.get(idx);
    }

    @Override
    public Integer getSize() {
        return this.getNumVertices();
    }

    @Override
    public Boolean containsEdge(Integer i, Integer j) {
        return this.hasEdge(i, j);
    }


    @Override
    public Character getVertexLabelChar(Integer i) {
        if(this.hasVertexWithIndex(i)) {
            return ' ';
        }
        return null;
    }

    @Override
    public Character getEdgeLabelChar(Integer i, Integer j) {
        if(this.hasEdge(i, j)) {
            return ' ';
        }
        return null;
    }

    public static void main(String[] args) {
        SparseGraph<String, String> g = new SparseGraph<>();
        SimpleGraphDrawer gd;
        g.addVertex("0");
        g.addVertex("1");
        g.addVertex("2");
        g.addVertex("3");
        
        gd = new SimpleGraphDrawer(g);
        System.out.println("Graph 1:\n" + gd.getGraphConsoleDrawing());
        
        g.addEdge(0, 1, "a");
        g.addEdge(1, 2, "b");
        g.addEdge(2, 3, "c");
        g.addEdge(3, 0, "d");
        
        gd = new SimpleGraphDrawer(g);
        System.out.println("Graph 2:\n" + gd.getGraphConsoleDrawing());
        
        g.addEdge(1, 3, "e");
        
        gd = new SimpleGraphDrawer(g);
        System.out.println("Graph 3:\n" + gd.getGraphConsoleDrawing());
        
        g.deleteVertex(2);
        
        gd = new SimpleGraphDrawer(g);
        System.out.println("Graph 4:\n" + gd.getGraphConsoleDrawing());
        
        g.deleteEdge(0, 2);
        
        gd = new SimpleGraphDrawer(g);
        System.out.println("Graph 5:\n" + gd.getGraphConsoleDrawing());
        
        g.deleteVertex(1);
        
        gd = new SimpleGraphDrawer(g);
        System.out.println("Graph 6:\n" + gd.getGraphConsoleDrawing());
    }
    
}
