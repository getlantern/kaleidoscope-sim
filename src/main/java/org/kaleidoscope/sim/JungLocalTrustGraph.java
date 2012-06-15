package org.kaleidoscope.sim; 

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.kaleidoscope.BasicTrustGraphAdvertisement;
import org.kaleidoscope.BasicTrustGraphNodeId;
import org.kaleidoscope.TrustGraphAdvertisement;
import org.kaleidoscope.TrustGraphNodeId;
import org.kaleidoscope.LocalTrustGraph;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;

/** 
 * JungLocalTrustGraph is an extension of LocalTrustGraph 
 * that maintains a JUNG Graph analog for use in simulation 
 * and visualization and tracks certain other simulation information.
 * 
 * Note/FIXME: this only tracks edges added via the LocalTrustGraph 
 * addRoute() methodS, not edges directly added to routing tables
 * or directed edges.
 * 
 */
class JungLocalTrustGraph extends LocalTrustGraph {


    public static enum NodeType {
        CENSORED, UNCENSORED, ADVERSARY
    }

    private Graph<TrustGraphNodeId, String> jungGraph;
    
    public JungLocalTrustGraph() {
        jungGraph = new UndirectedSparseGraph<TrustGraphNodeId, String>();
    }

    public Graph<TrustGraphNodeId, String> getJungGraph() {return jungGraph;}

    @Override
    public void addNode(final LocalTrustGraphNode node) {
        super.addNode(node);
        jungGraph.addVertex(node.getId());
    }

    @Override
    public void addRoute(final TrustGraphNodeId a, final TrustGraphNodeId b) {
        super.addRoute(a,b);
        addJungEdge(a,b);
    }

    @Override
    public void addRoute(final LocalTrustGraphNode a, final LocalTrustGraphNode b) {
        super.addRoute(a,b);
        addJungEdge(a.getId(), b.getId());
    }
    
    @Override 
    public LocalTrustGraph.LocalTrustGraphNode createNode(TrustGraphNodeId nodeId) {
        return new JungLocalTrustGraphNode(nodeId, this);
    }
    
    @Override
    public void clear() {
        for (TrustGraphNodeId v : jungGraph.getVertices()) {
            jungGraph.removeVertex(v);
        }
        super.clear();
    }
    
    protected void addJungEdge(TrustGraphNodeId a, TrustGraphNodeId b) {
        final String astr = a.toString(); 
        final String bstr = b.toString(); 

        final String name; 
        if (astr.compareTo(bstr) <= 0) {
            name = astr + "/" + bstr;
        }
        else {
            name = bstr + "/" + astr;
        }
        
        jungGraph.addEdge(name, a, b, EdgeType.UNDIRECTED);
    }


    public class JungLocalTrustGraphNode extends LocalTrustGraph.LocalTrustGraphNode {
        
        
        private NodeType nodeType;
        private boolean isBlocked;
        private List<TrustGraphNodeId> reached;
        
        public JungLocalTrustGraphNode(TrustGraphNodeId nodeId, LocalTrustGraph graph) {
            super(nodeId, graph);
            nodeType = NodeType.CENSORED;
            isBlocked = false;
            reached = new ArrayList<TrustGraphNodeId>();
        }

        @Override
        public void handleAdvertisement(TrustGraphAdvertisement message) {
            // extract node id from message payload
            BasicTrustGraphAdvertisement m =
                    (BasicTrustGraphAdvertisement) message;
            TrustGraphNodeId nodeId = new BasicTrustGraphNodeId(m.getPayload());
            JungLocalTrustGraphNode node =
                    (JungLocalTrustGraphNode) getNode(nodeId);

            // mark this node as being reached by the sender
            node.addReachedNode(getId());
            // if we are an adversary, mark the node we discovered as blocked
            if (nodeType == NodeType.ADVERSARY) {
                node.setBlocked(true);
            }

            super.handleAdvertisement(message);
        }
        
        public NodeType getType() {return nodeType;}
        public void setType(NodeType t) {nodeType=t;}
        
        public Color getColor() {
            
            if (nodeType == NodeType.ADVERSARY) {
                    return Color.RED;
            }
            else if (nodeType == NodeType.UNCENSORED) {
                if (isBlocked()) {
                    return Color.GRAY;
                }
                else {
                    return Color.WHITE;
                }
            }
            else { // CENSORED
                if (hasAdversarialRoute()) {
                    return Color.ORANGE;
                }
                else if (hasUncensoredRoute()) {
                    return Color.GREEN;
                }
                else {
                    return Color.BLACK;
                }
            }
        }
        
        @Override
        public void clear() {
            super.clear(); 
            isBlocked = false;
            reached.clear();
        }
        
        public boolean isBlocked() {
            return isBlocked;
        }
        
        public void setBlocked(boolean b) {
            isBlocked = b;
        }
        
        /** 
         * returns true if the node has gained 
         * knowledge of an uncensored node.
         */
        public boolean hasUncensoredRoute() {
            for (JungLocalTrustGraphNode n : getDiscoveredNodes()) {
                if (n.getType() == NodeType.UNCENSORED && !n.isBlocked()) {
                    return true;
                }
            }
            return false;
        }
        
        /**
         * returns true if the node has gained knowlege of an
         * adversarial node.
         */
        public boolean hasAdversarialRoute() {
            for (JungLocalTrustGraphNode n : getDiscoveredNodes()) {
                if (n.getType() == NodeType.ADVERSARY) {
                    return true;
                }
            }
            return false;
        }
        
        /** 
         * returns the list of nodes that this node has 
         * discovered by receiving advertisements.
         */
        public List<JungLocalTrustGraphNode> getDiscoveredNodes() {
            final LocalTrustGraph tg = getTrustGraph();
            Set<JungLocalTrustGraphNode> discovered =
                    new HashSet<JungLocalTrustGraphNode>();
            for (TrustGraphAdvertisement ad : getMessages()) {
                BasicTrustGraphAdvertisement message =
                        (BasicTrustGraphAdvertisement) ad;
                TrustGraphNodeId id = new BasicTrustGraphNodeId(message.getPayload());
                JungLocalTrustGraphNode node =
                    (JungLocalTrustGraphNode) tg.getNode(id);
                discovered.add(node);
            }
            return new ArrayList<JungLocalTrustGraphNode>(discovered);
        }
        
        public List<TrustGraphNodeId> getReachedNodesWithDupes() {
            return reached;
        }
        
        public List<TrustGraphNodeId> getReachedNodes() {
            return new ArrayList<TrustGraphNodeId>(new HashSet<TrustGraphNodeId>(reached));
        }
        
        public void addReachedNode(TrustGraphNodeId nodeId) {
            reached.add(nodeId);
        }
    }
    
    public void simulate() {
        clearNodeInfo(); 
        for (LocalTrustGraph.LocalTrustGraphNode n : getAllNodes()) {
            JungLocalTrustGraphNode node =
                (JungLocalTrustGraphNode) n;
                
            if (node.getType() == NodeType.UNCENSORED) {
                BasicTrustGraphAdvertisement message =
                    new BasicTrustGraphAdvertisement(node.getId(),
                        node.getId().toString(), 0);
                node.advertiseSelf(message);
            }
        }
    }
}