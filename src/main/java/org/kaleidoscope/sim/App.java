package org.kaleidoscope.sim; 

import edu.uci.ics.jung.algorithms.layout.BalloonLayout;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout2;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.PluggableGraphMouse;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.collections15.Transformer;
import org.kaleidoscope.BasicTrustGraphNodeId;
import org.kaleidoscope.LocalTrustGraph;
import org.kaleidoscope.TrustGraphNodeId;

public class App {
    
    private static void show() {
        final JFrame frame = new JFrame("Kaleidoscope Simulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        
        final JungLocalTrustGraph tg = new JungLocalTrustGraph();
        final Graph<TrustGraphNodeId, String> g = tg.getJungGraph();

        // XXX 
        LocalTrustGraph.LocalTrustGraphNode last = tg.addNode();
        for (int i = 0; i < 10; i++) {
            LocalTrustGraph.LocalTrustGraphNode cur = tg.addNode();
            tg.addRoute(last,cur);
            last = cur;
        }
        tg.growToivonenSocialNetwork(100);
        
        Layout layout = new ISOMLayout(g);
        //Layout layout = new FRLayout(g);
        //Layout layout = new KKLayout(g);
        
        layout.setSize(new Dimension(1024,1024));
        final VisualizationViewer<TrustGraphNodeId, String> vv =
            new VisualizationViewer<TrustGraphNodeId, String>(layout);
            
        Transformer<TrustGraphNodeId,Paint> vertexPaint = 
            new Transformer<TrustGraphNodeId,Paint>() {
                @Override
                public Paint transform(TrustGraphNodeId nodeId) {
                    JungLocalTrustGraph.JungLocalTrustGraphNode node =
                        (JungLocalTrustGraph.JungLocalTrustGraphNode)
                            tg.getNode(nodeId);
                    return node.getColor();
                }
            };
        vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);

        Transformer<TrustGraphNodeId,String> vertexLabel =
            new Transformer<TrustGraphNodeId,String>() {
                @Override
                public String transform(TrustGraphNodeId nodeId) {
                    JungLocalTrustGraph.JungLocalTrustGraphNode node =
                        (JungLocalTrustGraph.JungLocalTrustGraphNode) tg.getNode(nodeId);
                        
                    if (node.getType() == JungLocalTrustGraph.NodeType.UNCENSORED) {
                        return "" + node.getReachedNodes().size() + "/" + node.getReachedNodesWithDupes().size();
                    }
                    else {
                        int sz = node.getDiscoveredNodes().size();
                        if (sz > 0) {
                            return "" + sz;
                        }
                        else {
                            return "";
                        }
                    }
                }
            };
        vv.getRenderContext().setVertexLabelTransformer(vertexLabel);

        // vv.getRenderContext().setVertexFillPaintTransformer(
        //     new PickableVertexPaintTransformer<TrustGraphNodeId>(
        //         vv.getPickedVertexState(), Color.red, Color.yellow));
        
        final PluggableGraphMouse graphMouse = new PluggableGraphMouse();
        graphMouse.add(new ToggleTypeMousePlugin(tg));
        graphMouse.add(new TranslatingGraphMousePlugin(InputEvent.BUTTON1_MASK));
        graphMouse.add(new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0, 1.1f, 0.9f));

        //final DefaultModalGraphMouse<TrustGraphNodeId, String> graphMouse =
        //    new DefaultModalGraphMouse<TrustGraphNodeId,String>();
        vv.setGraphMouse(graphMouse);

        final JPanel jp = new JPanel();
        jp.setBackground(Color.WHITE);
        jp.setLayout(new BorderLayout());
        jp.add(vv);

        final ScalingControl scaler = new CrossoverScalingControl();
        final JButton zoomIn = new JButton("zoom in");
        zoomIn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1.1f, vv.getCenter());
            }
        });
        final JButton zoomOut = new JButton("zoom out");
        zoomOut.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1/1.1f, vv.getCenter());
            }
        });
    
        final JPanel controls = new JPanel();
        jp.add(controls, BorderLayout.NORTH);
        
        controls.add(zoomIn);
        controls.add(zoomOut);
        
        frame.getContentPane().add(jp);
        frame.pack();
        frame.setVisible(true);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                show();
            }
        });
    }
}