 /*
  *   casmi examples
  *   http://casmi.github.com/
  *   Copyright (C) 2012, Xcoo, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package casmi;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JScrollPane;
 import javax.swing.JTree;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 
 /**
  * Example launcher.
  * 
  * @author T. Takeuchi
  */
 public class AllExample extends JFrame {
 
     static final String[][] EXAMPLES = {
         { "casmi", "" },
         { "casmi.CursorExample",                               "Cursor Example"              },
         { "casmi.FPSExample",                                  "FPS Example"                 },
         { "casmi.FullScreenExample",                           "Full Screen Example"         },
         { "casmi.KeyboardExample",                             "Keyboard Example"            },
         { "casmi.KeyboardSimpleExample",                       "Simple Keyboard Example"     },
         { "casmi.MouseExample",                                "Mouse Example"               },
         { "casmi.MouseWheelExample",                           "Mouse Wheel Example"         },
         { "casmi.PopupMenuExample",                            "Popup Menu Example"          },
         { "casmi.ScreenshotExample",                           "Screenshot Example"          },
         { "casmi.TrackballExample",                            "Trackball Example"           },
 
         { "casmi.chart", "" },
         { "casmi.chart.DynamicBarChartExample",                "Dynamic BarChart Example"    },
         { "casmi.chart.DynamicCircleChartExample",             "Dynamic CircleChart Example" },
         { "casmi.chart.DynamicLineChartExample",               "Dynamic LineChart Example"   },
 
         { "casmi.graphics", "" },
         { "casmi.graphics.AlphaExample",                       "Alpha Example"               },
         { "casmi.graphics.ArcExample",                         "Arc Example"                 },
         { "casmi.graphics.ArrowExample",                       "Arrow Example"               },
         { "casmi.graphics.BackGroundColorExample",             "BackGround Color Example"    },
         { "casmi.graphics.BezierExample",                      "Bezier Example"              },
         { "casmi.graphics.BoxExample",                         "Box Example"                 },
         { "casmi.graphics.BoxTextureExample",                  "Box Texture Example"         },
         { "casmi.graphics.CameraExample",                      "Camera Example"              },
         { "casmi.graphics.CircleExample",                      "Circle Example"              },
         { "casmi.graphics.ConeExample",                        "Cone Example"                },
         { "casmi.graphics.CurveExample",                       "Curve Example"               },
         { "casmi.graphics.EllipseExample",                     "Ellipse Example"             },
         { "casmi.graphics.ImageModeExample",                   "ImageMode Example"           },
         { "casmi.graphics.LightExample",                       "Light Example"               },
         { "casmi.graphics.LineExample",                        "Line Example"                },
         { "casmi.graphics.LinesExample",                       "Lines Example"               },
         { "casmi.graphics.MaskExample",                        "Mask Example"                },
         { "casmi.graphics.MaterialExample",                    "Material Example"            },
         { "casmi.graphics.PointExample",                       "Point Example"               },
         { "casmi.graphics.PolygonExample",                     "Polygon Example"             },
         { "casmi.graphics.QuadExample",                        "Quad Example"                },
         { "casmi.graphics.QuadTextureExample",                 "Quad Texture Example"        },
         { "casmi.graphics.RectExample",                        "Rect Example"                },
         { "casmi.graphics.RotateExample",                      "Rotate Example"              },
        { "casmi.graphics.RoundrectExample",                   "Roundrect Example"           },
         { "casmi.graphics.SphereExample",                      "Sphere Example"              },
         { "casmi.graphics.SphereTextureExample",               "Sphere Texture Example"      },
         { "casmi.graphics.TextBoxExample",                     "TextBox Example"             },
         { "casmi.graphics.TextExample",                        "Text Example 1"              },
         { "casmi.graphics.TextExample2",                       "Text Example 2"              },
         { "casmi.graphics.TextureExample",                     "Texture Example"             },
         { "casmi.graphics.TextureMaskExample",                 "Texture Mask Example"        },
         { "casmi.graphics.TriangleExample",                    "Triangle Example"            },
    
         { "casmi.graphics.color", "" },
         { "casmi.graphics.color.ColorSetExample",              "ColorSet Example"            },
         { "casmi.graphics.color.GrayColorExample",             "GrayColor Example"           },
         { "casmi.graphics.color.LerpColorExample",             "LerpColor Example"           },
         
         { "casmi.graphics.font", "" },
         { "casmi.graphics.font.FontExample",                   "Font Example"                },
         
         { "casmi.graphics.gradation", "" },
         { "casmi.graphics.gradation.GradationBezierExample",   "Gradation Bezier Example"    },
         { "casmi.graphics.gradation.GradationBoxExample",      "Gradation Box Example"       },
         { "casmi.graphics.gradation.GradationExample",         "Gradation Example"           },
         
         { "casmi.graphics.group", "" },
         { "casmi.graphics.group.GroupColorExample",            "Group Color Example"         },
         { "casmi.graphics.group.GroupExample",                 "Group Example"               },
         
         { "casmi.graphics.mouseover", "" },
         { "casmi.graphics.mouseover.MouseOverColorSetExample", "MouseOver ColorSet Example"  },
         { "casmi.graphics.mouseover.MouseOverExample",         "MouseOver Example"           },
         { "casmi.graphics.mouseover.MouseOverGroupExample",    "MouseOver Group Example"     },
         { "casmi.graphics.mouseover.MouseOverTextExample",     "MouseOver Text Example"      },
         
         { "casmi.graphics.object", "" },
         { "casmi.graphics.object.ObjectExample",               "Object Example"              },
         { "casmi.graphics.object.RemoveExample",               "Remove Example"              },
         
         { "casmi.sound", "" },
         { "casmi.sound.SoundExample",                          "Sound Example"               },
         
         { "casmi.timeline", "" },
         { "casmi.timeline.TimelineActiveExample",              "Timeline Active Example"     },
         { "casmi.timeline.TimelineExample",                    "Timeline Example"            },
         
         { "casmi.tween", "" },
         { "casmi.tween.TweenCursorExample",                    "Tween Cursor Example"        },
         { "casmi.tween.TweenEquationsExample",                 "Tween Equations Example"     },
         { "casmi.tween.TweenExample",                          "Tween Example"               },
         { "casmi.tween.TweenGroupRepeatExample",               "Tween Group Repeat Example"  },
         { "casmi.tween.TweenParallelExample",                  "Tween Parallel Example"      },
         { "casmi.tween.TweenRepeatExample",                    "Tween Repeat Example"        },
         { "casmi.tween.TweenSimpleExample",                    "Tween Simple Example"        },
         { "casmi.tween.TweenVertexExample",                    "Tween Vertex Example"        }
     };
     
     JTree tree;
     JFrame currentApplication;
     
     public AllExample() {
         super();
         
         setTitle("Example Launcher");
         setBounds(50, 50, 300, 600);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
         DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Examples");
 
         DefaultMutableTreeNode node = null;
         for (String[] s : EXAMPLES) {
             if (s[1].isEmpty()) {
                 node = new DefaultMutableTreeNode(s[0]);
                 rootNode.add(node);
             } else {
                 node.add(new DefaultMutableTreeNode(s[1]));
             }
         }
         
         DefaultTreeModel model = new DefaultTreeModel(rootNode);
         tree = new JTree(model);
         
         JScrollPane sp = new JScrollPane();
         sp.getViewport().setView(tree);
         
         JButton button = new JButton("Run");
         button.addActionListener(new ActionListener() {
             
             @Override
             public void actionPerformed(ActionEvent arg0) {                
                 TreePath path = tree.getSelectionPath();
                 if (path == null) {
                     return;
                 }
                 
                 if (currentApplication != null) {
                     currentApplication.dispose();
                 }               
                 
                 String title = path.getLastPathComponent().toString();
                 
                 for (String[] s : EXAMPLES) {
                     if (title.equals(s[1])) {
                         AppletRunner.run(s[0], s[1]);
                         AppletRunner.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                         currentApplication = AppletRunner.frame;
                         break;
                     }
                }              
             }
         });
         
         getContentPane().add(sp, BorderLayout.CENTER);
         getContentPane().add(button, BorderLayout.SOUTH);
         
         setVisible(true);
     }
     
     public static void main(String[] args) {
         new AllExample();
     }
 }
