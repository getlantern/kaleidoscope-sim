package org.kaleidoscope.sim; 

import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.control.AbstractGraphMousePlugin;
import edu.uci.ics.jung.visualization.VisualizationViewer;

import org.kaleidoscope.TrustGraphNodeId;

class ToggleTypeMousePlugin extends AbstractGraphMousePlugin
    implements MouseListener, MouseMotionListener {
    
    private JungLocalTrustGraph trustGraph;
    
    public ToggleTypeMousePlugin(JungLocalTrustGraph tg) {
        super(InputEvent.BUTTON1_MASK);
        trustGraph = tg;
    }
    
    public void mousePressed(MouseEvent e) {
        down = e.getPoint();
        VisualizationViewer<TrustGraphNodeId,String> vv = (VisualizationViewer)e.getSource();
        GraphElementAccessor<TrustGraphNodeId,String> pickSupport = vv.getPickSupport();
        // PickedState<V> pickedVertexState = vv.getPickedVertexState();
        // PickedState<E> pickedEdgeState = vv.getPickedEdgeState();

        Layout<TrustGraphNodeId,String> layout = vv.getGraphLayout();
        if(e.getModifiers() == modifiers) {
            TrustGraphNodeId vertex = pickSupport.getVertex(layout, down.getX(), down.getY());
            if (vertex != null) {
                clicked(vertex);
                e.consume();
                vv.repaint();
            }
        }
    }
    
    public void clicked(TrustGraphNodeId vertex) {
        JungLocalTrustGraph.JungLocalTrustGraphNode node = 
            (JungLocalTrustGraph.JungLocalTrustGraphNode)
            trustGraph.getNode(vertex);

        if (node.getType() == JungLocalTrustGraph.NodeType.CENSORED) {
            node.setType(JungLocalTrustGraph.NodeType.UNCENSORED);
        }
        else if (node.getType() == JungLocalTrustGraph.NodeType.UNCENSORED) {
            node.setType(JungLocalTrustGraph.NodeType.ADVERSARY);
        }
        else if (node.getType() == JungLocalTrustGraph.NodeType.ADVERSARY) {
            node.setType(JungLocalTrustGraph.NodeType.CENSORED);
        }
        
        trustGraph.simulate();
    }
    
    // etc
    public void mouseReleased(MouseEvent e) {}
    public void mouseDragged(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}
   
}