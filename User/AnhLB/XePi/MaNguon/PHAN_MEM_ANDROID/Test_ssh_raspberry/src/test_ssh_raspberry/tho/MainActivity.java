package test_ssh_raspberry.tho;


import com.jcraft.jsch.*;
import android.view.*;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.annotation.SuppressLint;
import android.app.*;
import android.content.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.*;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;


public class MainActivity extends Activity {
	public String hostname, portnum,topi,tentb,ndtb,dataline;
	public String frompi= "";
	public Socket socket;
	String prefname="my_data"; 
	Boolean TTluu=false,khoacheo=true,ttkt=false;
    SharedPreferences prefs = null;
    ChannelExec channel;
    Session session;
    BufferedReader in;
    Button butcn,butdcn,butsh,butrb,buttd,butdk,butdoline,butsvl,butsvr,butktline;
    Button tocdo1,tocdo2,tocdo3,tocdo4,tocdo5;
    ImageView len,xuong,trai,phai;
    EditText edip,eduse,edpass;
    Boolean paused = false;
    TextView txtTT,txthostname,txtktline;
    @SuppressLint("NewApi") @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadTabs();
        Intent myIntent = new Intent(this, BatdauActivity.class);
		startActivity(myIntent);	
       butcn = (Button)findViewById(R.id.buttonconnect);
       butdcn = (Button)findViewById(R.id.buttondisconnect);
       butsh = (Button)findViewById(R.id.buttonshutdown);
       butrb = (Button)findViewById(R.id.buttonreboot);
       len =(ImageView)findViewById(R.id.imglen);
       xuong =(ImageView)findViewById(R.id.imgxuong);
       trai =(ImageView)findViewById(R.id.imgtrai);
       phai =(ImageView)findViewById(R.id.imgphai);
       edip=(EditText)findViewById(R.id.edittextip);
       eduse=(EditText)findViewById(R.id.editextusername);
       edpass=(EditText)findViewById(R.id.edittextpassword);
       buttd = (Button)findViewById(R.id.nuttd);
       butdk = (Button)findViewById(R.id.nutdk);
       butdoline = (Button)findViewById(R.id.nutdoline);
       butsvl = (Button)findViewById(R.id.nutsvl);
       butsvr = (Button)findViewById(R.id.nutsvr);
       butktline = (Button)findViewById(R.id.nutktline);
       txtTT = (TextView)findViewById(R.id.texttrangthai); 
       txthostname = (TextView)findViewById(R.id.texthostname); 
       txtktline = (TextView)findViewById(R.id.textktline); 
       tocdo1 = (Button)findViewById(R.id.nuttocdo1);
       tocdo2 = (Button)findViewById(R.id.nuttocdo2);
       tocdo3 = (Button)findViewById(R.id.nuttocdo3);
       tocdo4 = (Button)findViewById(R.id.nuttocdo4);
       tocdo5 = (Button)findViewById(R.id.nuttocdo5);
		len.setEnabled(false);
		xuong.setEnabled(false);
		phai.setEnabled(false);
		trai.setEnabled(false);
		buttd.setEnabled(false);
		butdk.setEnabled(false);
		butdoline.setEnabled(false);
		butsvl.setEnabled(false);
		butsvr.setEnabled(false);
		butsh.setEnabled(false);
		butrb.setEnabled(false);
		butdcn.setEnabled(false);
		tocdo1.setEnabled(false);
		tocdo2.setEnabled(false);
		tocdo3.setEnabled(false);
		tocdo4.setEnabled(false);
		tocdo5.setEnabled(false);
		txtTT.setText("Chưa có kết nối!");
	    butdk.setOnClickListener(new View.OnClickListener() {
	   		
	   		@Override
	   		public void onClick(View v) {
	   			// TODO Auto-generated method stub
	   			try {
	   				guilenh("ps aux | grep 'dolinesang.py' | grep -v grep | awk '{print $2}' | xargs sudo kill -9");
					Thread.sleep(500);
	   				guilenh("ps aux | grep 'tudong.py' | grep -v grep | awk '{print $2}' | xargs sudo kill -9");
					Thread.sleep(500);
					guilenh("python dieukhien.py");
					Thread.sleep(500);
			   	    MyClientTask myClientTask = new MyClientTask(edip.getText().toString(),Integer.parseInt("5005"));
				   	myClientTask.execute();
				   	butdk.setEnabled(false);
				   	buttd.setEnabled(true);
				   	butdoline.setEnabled(true);
					len.setEnabled(true);
					xuong.setEnabled(true);
					phai.setEnabled(true);
					trai.setEnabled(true);
    				butdoline.setEnabled(true);
    				butsvl.setEnabled(true);
    				butsvr.setEnabled(true);
    				tocdo1.setEnabled(false);
    				tocdo2.setEnabled(true);
    				tocdo3.setEnabled(true);
    				tocdo4.setEnabled(true);
    				tocdo5.setEnabled(true);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
	   	   
	   		}
	   	});		

	   butktline.setOnClickListener(new View.OnClickListener() {
	   		
	   		@Override
	   		public void onClick(View v) {
	   			// TODO Auto-generated method stub
	   			ttkt=!ttkt;
	   			if (ttkt==true)
	   			{
	   				butktline.setText("Dừng");
	   			}
	   			else
	   			{
	   				butktline.setText("Bắt đầu");
	   			}
	   		}
	   	});        
       butcn.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			TTluu= true;
			ConnectSSH();
		}
	});

       butsh.setOnClickListener(new View.OnClickListener() {
      		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			shutdown();
		}
	});
       butrb.setOnClickListener(new View.OnClickListener() {
      		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			reboot();
		}
	});



    }
    class MyClientTask extends AsyncTask<Void, Void, Void> {
		String response;
		public int speed=0;
		boolean i= false;
		MyClientTask(String addr, int Port) {

		}
		
		@Override
		protected Void doInBackground(Void... arg0) {
			
			 try {
				 
				 final  Socket socket = new Socket(edip.getText().toString(),Integer.parseInt("5005"));
				 PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
//				 InputStreamReader in = new InputStreamReader(socket.getInputStream());
				 topi = "Xin chao Raspberry Pi!";
//				 frompi = in.;
//				 Toast toast=Toast.makeText(getApplicationContext(),frompi , Toast.LENGTH_SHORT);
//				 toast.show();
				 out.println(topi);
			      len.setOnTouchListener(new OnTouchListener() {
			  		
			  		@Override
			  		public boolean onTouch(View v, MotionEvent event) {
			  			// TODO Auto-generated method stub
			  			switch (event.getAction()) {
			              case MotionEvent.ACTION_DOWN: {
			            	  PrintWriter out;
							try {
								out = new PrintWriter(socket.getOutputStream(),true);
								 out.println("TOI");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();	
							}
			            	 
			              	len.setImageResource(R.drawable.len1);
			              	return true;
			              }


			              case MotionEvent.ACTION_UP: {
			              	//takePhoto();
			              	len.setImageResource(R.drawable.len);
			              	
			            	  PrintWriter out;
							try {
								out = new PrintWriter(socket.getOutputStream(),true);
								 out.println("DUNG");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();	
							}
			            	 
			              	return true;
			                
			              }

			              }
			  			return false;
			  		}
			  	});
			         xuong.setOnTouchListener(new OnTouchListener() {
			     		
			  		@Override
			  		public boolean onTouch(View v, MotionEvent event) {
			  			// TODO Auto-generated method stub
			  			switch (event.getAction()) {
			              case MotionEvent.ACTION_DOWN: {
			            	  PrintWriter out;
								try {
									out = new PrintWriter(socket.getOutputStream(),true);
									 out.println("LUI");
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();	
								}
			              	xuong.setImageResource(R.drawable.xuong1);
			              	return true;
			              }

			              case MotionEvent.ACTION_UP: {
			              	//takePhoto();
			              	xuong.setImageResource(R.drawable.xuong);
			              	
			              	PrintWriter out;
							try {
								out = new PrintWriter(socket.getOutputStream(),true);
								 out.println("DUNG");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();	
							}
			              	return true;
			                
			              }

			              }
			  			return false;
			  		}
			  	});
			         trai.setOnTouchListener(new OnTouchListener() {
			        		
			  		@Override
			  		public boolean onTouch(View v, MotionEvent event) {
			  			// TODO Auto-generated method stub
			  			switch (event.getAction()) {
			              case MotionEvent.ACTION_DOWN: {
			            	  PrintWriter out;
								try {
									out = new PrintWriter(socket.getOutputStream(),true);
									 out.println("TRAI");
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();	
								}
			              	trai.setImageResource(R.drawable.trai1);
			              	return true;
			              }

			              case MotionEvent.ACTION_UP: {
			              	//takePhoto();
			              	trai.setImageResource(R.drawable.trai);
			              	
			              	PrintWriter out;
							try {
								out = new PrintWriter(socket.getOutputStream(),true);
								 out.println("DUNG");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();	
							}
			              	return true;
			                
			              }

			              }
			  			return false;
			  		}
			  	});
			  		
			         phai.setOnTouchListener(new OnTouchListener() {
			        		
			  		@Override
			  		public boolean onTouch(View v, MotionEvent event) {
			  			// TODO Auto-generated method stub
			  			switch (event.getAction()) {
			              case MotionEvent.ACTION_DOWN: {
			            	  PrintWriter out;
								try {
									out = new PrintWriter(socket.getOutputStream(),true);
									 out.println("PHAI");
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();	
								}
			              	phai.setImageResource(R.drawable.phai1);
			              	return true;
			              }

			              case MotionEvent.ACTION_UP: {
			              	//takePhoto();
			              	phai.setImageResource(R.drawable.phai);
			              	
			              	PrintWriter out;
							try {
								out = new PrintWriter(socket.getOutputStream(),true);
								 out.println("DUNG");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();	
							}
			              	return true;
			                
			              }

			              }
			  			return false;
			  		}
			  	});
			         butsvl.setOnClickListener(new View.OnClickListener() {
					   		
					   		@Override
					   		public void onClick(View v) {
					   			// TODO Auto-generated method stub
				              	PrintWriter out;
								try {
									out = new PrintWriter(socket.getOutputStream(),true);
									 out.println("SVTRAI");
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();	
								}
					   		}
					   	});	
			         tocdo1.setOnClickListener(new View.OnClickListener() {
					   		
					   		@Override
					   		public void onClick(View v) {
					   			// TODO Auto-generated method stub
				              	PrintWriter out;
								try {
									out = new PrintWriter(socket.getOutputStream(),true);
									 out.println("TOCDO1");
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();	
								}
								tocdo1.setEnabled(false);
								tocdo2.setEnabled(true);
								tocdo3.setEnabled(true);
								tocdo4.setEnabled(true);
								tocdo5.setEnabled(true);
					   		}
					   	});	
			         tocdo2.setOnClickListener(new View.OnClickListener() {
					   		
					   		@Override
					   		public void onClick(View v) {
					   			// TODO Auto-generated method stub
				              	PrintWriter out;
								try {
									out = new PrintWriter(socket.getOutputStream(),true);
									 out.println("TOCDO2");
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();	
								}
								tocdo1.setEnabled(true);
								tocdo2.setEnabled(false);
								tocdo3.setEnabled(true);
								tocdo4.setEnabled(true);
								tocdo5.setEnabled(true);
					   		}
					   	});	
			         tocdo3.setOnClickListener(new View.OnClickListener() {
					   		
					   		@Override
					   		public void onClick(View v) {
					   			// TODO Auto-generated method stub
				              	PrintWriter out;
								try {
									out = new PrintWriter(socket.getOutputStream(),true);
									 out.println("TOCDO3");
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();	
								}
								tocdo1.setEnabled(true);
								tocdo2.setEnabled(true);
								tocdo3.setEnabled(false);
								tocdo4.setEnabled(true);
								tocdo5.setEnabled(true);
					   		}
					   	});		
			         tocdo4.setOnClickListener(new View.OnClickListener() {
					   		
					   		@Override
					   		public void onClick(View v) {
					   			// TODO Auto-generated method stub
				              	PrintWriter out;
								try {
									out = new PrintWriter(socket.getOutputStream(),true);
									 out.println("TOCDO4");
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();	
								}
								tocdo1.setEnabled(true);
								tocdo2.setEnabled(true);
								tocdo3.setEnabled(true);
								tocdo4.setEnabled(false);
								tocdo5.setEnabled(true);
					   		}
					   	});	
			         tocdo5.setOnClickListener(new View.OnClickListener() {
					   		
					   		@Override
					   		public void onClick(View v) {
					   			// TODO Auto-generated method stub
				              	PrintWriter out;
								try {
									out = new PrintWriter(socket.getOutputStream(),true);
									 out.println("TOCDO5");
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();	
								}
								tocdo1.setEnabled(true);
								tocdo2.setEnabled(true);
								tocdo3.setEnabled(true);
								tocdo4.setEnabled(true);
								tocdo5.setEnabled(false);
					   		}
					   	});				      
			         butsvr.setOnClickListener(new View.OnClickListener() {
					   		
					   		@Override
					   		public void onClick(View v) {
					   			// TODO Auto-generated method stub
				              	PrintWriter out;
								try {
									out = new PrintWriter(socket.getOutputStream(),true);
									 out.println("SVPHAI");
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();	
								}
					   		}
					   	});		
			 	    buttd.setOnClickListener(new View.OnClickListener() {
				   		
				   		@Override
				   		public void onClick(View v) {
				   			// TODO Auto-generated method stub
				   			try {
				   				try {
									socket.close();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
				   				guilenh("ps aux | grep 'dieukhien.py' | grep -v grep | awk '{print $2}' | xargs sudo kill -9");
								Thread.sleep(500);
				   				guilenh("ps aux | grep 'dolinesang.py' | grep -v grep | awk '{print $2}' | xargs sudo kill -9");
								Thread.sleep(500);
								guilenh("python tudong.py");
								Thread.sleep(500);
								butdk.setEnabled(true);
								buttd.setEnabled(false);
								butdoline.setEnabled(true);
								len.setEnabled(false);
								xuong.setEnabled(false);
								phai.setEnabled(false);
								trai.setEnabled(false);
			    				butsvl.setEnabled(false);
			    				butsvr.setEnabled(false);
			    				tocdo1.setEnabled(false);
			    				tocdo2.setEnabled(false);
			    				tocdo3.setEnabled(false);
			    				tocdo4.setEnabled(false);
			    				tocdo5.setEnabled(false);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
				   		}
				   	});	
			 	    butdoline.setOnClickListener(new View.OnClickListener() {
				   		
				   		@Override
				   		public void onClick(View v) {
				   			// TODO Auto-generated method stub
				   			try {
				   				try {
									socket.close();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
				   				guilenh("ps aux | grep 'dieukhien.py' | grep -v grep | awk '{print $2}' | xargs sudo kill -9");
								Thread.sleep(500);
				   				guilenh("ps aux | grep 'tudong.py' | grep -v grep | awk '{print $2}' | xargs sudo kill -9");
								Thread.sleep(500);
								guilenh("python dolinesang.py");
								Thread.sleep(500);
								butdk.setEnabled(true);
								buttd.setEnabled(true);
								butdoline.setEnabled(false);
								len.setEnabled(false);
								xuong.setEnabled(false);
								phai.setEnabled(false);
								trai.setEnabled(false);
			    				butsvl.setEnabled(false);
			    				butsvr.setEnabled(false);
			    				tocdo1.setEnabled(false);
			    				tocdo2.setEnabled(false);
			    				tocdo3.setEnabled(false);
			    				tocdo4.setEnabled(false);
			    				tocdo5.setEnabled(false);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
				   		}
				   	});	
			        butdcn.setOnClickListener(new View.OnClickListener() {
			       		
			    		@Override
			    		public void onClick(View v) {
			    			// TODO Auto-generated method stub
							try {
								try {
			    					socket.close();
			    				} catch (IOException e) {
			    					// TODO Auto-generated catch block
			    					e.printStackTrace();
			    				}
								guilenh("ps aux | grep 'dieukhien.py' | grep -v grep | awk '{print $2}' | xargs sudo kill -9");
								Thread.sleep(500);
								guilenh("ps aux | grep 'tudong.py' | grep -v grep | awk '{print $2}' | xargs sudo kill -9");
								Thread.sleep(500);
								guilenh("ps aux | grep 'dolinesang.py' | grep -v grep | awk '{print $2}' | xargs sudo kill -9");
								Thread.sleep(500);
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
			    			
			    				txtTT.setText("Chưa có kết nối!");
								butdk.setEnabled(false);
								buttd.setEnabled(false);
			    				len.setEnabled(false);
			    				xuong.setEnabled(false);
			    				phai.setEnabled(false);
			    				trai.setEnabled(false);
			    				butdoline.setEnabled(false);
			    				butsvl.setEnabled(false);
			    				butsvr.setEnabled(false);
			    				butsh.setEnabled(false);
			    				butrb.setEnabled(false);
			    				butdcn.setEnabled(false);
			    				butcn.setEnabled(true);
								tocdo1.setEnabled(false);
								tocdo2.setEnabled(false);
								tocdo3.setEnabled(false);
								tocdo4.setEnabled(false);
								tocdo5.setEnabled(false);
			    				DisconnectSSH();
			    		}
			    	});
			 } catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				return null;
			}
			}

	public void loadTabs() {
		// TODO Auto-generated method stub
		final TabHost tab = (TabHost) findViewById(android.R.id.tabhost);
		tab.setup();
		TabHost.TabSpec spec;
		spec=tab.newTabSpec("t1");
		spec.setContent(R.id.tab1);
		spec.setIndicator("Điều khiển");
		tab.addTab(spec);
		
		spec=tab.newTabSpec("t2");
		spec.setContent(R.id.tab2);
		spec.setIndicator("Cài đặt");
		tab.addTab(spec);

		spec=tab.newTabSpec("t3");
		spec.setContent(R.id.tab3);
		spec.setIndicator("Line");
		tab.addTab(spec);
		
		tab.setCurrentTab(0);
		
		
		
		
	}
    public boolean isOnline() {
        ConnectivityManager conMgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

        if (netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()) {
            return false;
        }
        return true;
    }
    public void StartUpdateLoop() {

        new Thread(new Runnable() {
            @Override
            public void run() {
            	 DecimalFormat df;
                try {
                    while (isOnline() && session.isConnected()) {
                        while (!paused) {

                            try {
                            	if (txthostname.getText().equals("---"))
                            	{
                            	tentb = ExecuteCommand("hostname -f");
                            	}
                            	df = new DecimalFormat("0.0");
                                String cputemp_str = ExecuteCommand("cat /sys/class/thermal/thermal_zone0/temp");

                                if (!cputemp_str.isEmpty()) {
                                    ndtb = df.format(Float.parseFloat(cputemp_str) / 1000) + "°C";
                                } else {
                                    ndtb = "Không hoạt động";
                                }
                                if(ttkt==true)
                                {
                                	dataline = ExecuteCommand("python ktline.py");
                                }
                                else
                                {
                                	dataline="";
                                }
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                    	txthostname.setText("Đã kết nối với "+tentb+" | Nhiệt độ:"+ndtb);
                                    	txtktline.setText(dataline);
                                    	if (khoacheo==true)
                                    	{
                                    	Toast.makeText(getBaseContext(),"Kết nối thành công!",Toast.LENGTH_LONG).show();  
                                    	txtTT.setText("Bảng Điều Khiển");
                                		buttd.setEnabled(true);
                                		butdk.setEnabled(true);
                                		butdoline.setEnabled(true);
                                		butsh.setEnabled(true);
                                		butrb.setEnabled(true);
                                		butdcn.setEnabled(true);
                                		butcn.setEnabled(false);

                        	   			// TODO Auto-generated method stub
                        	   			try {
                        	   				guilenh("ps aux | grep 'tudong.py' | grep -v grep | awk '{print $2}' | xargs sudo kill -9");
                        					Thread.sleep(500);
                        	   			guilenh("python dieukhien.py");
                        					Thread.sleep(500);
                        			   	    MyClientTask myClientTask = new MyClientTask(edip.getText().toString(),Integer.parseInt("5005"));
                        				   	myClientTask.execute();
                        				   	butdk.setEnabled(false);
                        				   	buttd.setEnabled(true);
                        					len.setEnabled(true);
                        					xuong.setEnabled(true);
                        					phai.setEnabled(true);
                        					trai.setEnabled(true);
                            				butdoline.setEnabled(true);
                            				butsvl.setEnabled(true);
                            				butsvr.setEnabled(true);
            								tocdo1.setEnabled(false);
            								tocdo2.setEnabled(true);
            								tocdo3.setEnabled(true);
            								tocdo4.setEnabled(true);
            								tocdo5.setEnabled(true);
                        				} catch (InterruptedException e) {
                        					// TODO Auto-generated catch block
                        					e.printStackTrace();
                        				} 
                        	   	   
                        	   		
                                		khoacheo=false;
                                    	}
                                    }
                                });
                                Thread.sleep(500);

                            } catch (Exception e) {
                                ThrowException(e.getMessage());
                            }
                        }
                    }

                    DisconnectSSH();
                    ThrowException("Không thể kết nối tới raspberry!");
                } catch (Exception e) {
                    ThrowException(e.getMessage());
                }
            }
        }).start();
    }
    public void ConnectSSH() {
        khoacheo=true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSch jsch = new JSch();
                    session = jsch.getSession(eduse.getText().toString(), edip.getText().toString(), 22);
                    session.setPassword(edpass.getText().toString());
                    Properties config = new Properties();
                    config.put("StrictHostKeyChecking", "no");
                    session.setConfig(config);
                    session.connect();
                    StartUpdateLoop();
                } catch (final Exception e) {
                    ThrowException(e.getMessage());
                }
            }
        }).start();
    }

    public void DisconnectSSH() {
        channel.disconnect();
        session.disconnect();
        Toast.makeText(getBaseContext(),"Đã ngắt kết nối!",Toast.LENGTH_LONG).show();
    }

    public String ExecuteCommand(String command) {
        try {
            if (session.isConnected()) {
                channel = (ChannelExec) session.openChannel("exec");
                in = new BufferedReader(new InputStreamReader(channel.getInputStream()));

                String username = eduse.getText().toString();
                if (!username.equals("root")) {
                    command = "sudo " + command;
                }

                channel.setCommand(command);
                channel.connect();

                StringBuilder builder = new StringBuilder();

                String line = null;
                while ((line = in.readLine()) != null) {
                    builder.append(line).append(System.getProperty("line.separator"));
                }

                String output = builder.toString();
                if (output.lastIndexOf("\n") > 0) {
                    return output.substring(0, output.lastIndexOf("\n"));
                } else {
                    return output;
                }
            }
        } catch (Exception e) {
            ThrowException(e.getMessage());
        }

        return "";
    }
public String guilenh(String command) {
        try {
            if (session.isConnected()) {
                channel = (ChannelExec) session.openChannel("exec");
                String username = eduse.getText().toString();
                if (!username.equals("root")) {
                    command = "sudo " + command;
                }
                channel.setCommand(command);
                channel.connect();

            }
        } catch (Exception e) {
            ThrowException(e.getMessage());
        }

        return "";
    }
    public void ThrowException(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Lỗi");
                builder.setMessage(msg);
                builder.setNegativeButton("Đóng", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                builder.show();
            }
        });
    }



    public void shutdown() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận")
                .setMessage("Bạn có muốn tắt raspberry không?")
                .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ExecuteCommand("shutdown -h now");
                        DisconnectSSH();
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                })
                .setNegativeButton("Không", null)
                .show();

    }

    public void reboot() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận")
                .setMessage("Bạn có muốn khởi động lại raspberry không?")
                .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ExecuteCommand("shutdown -r now");
                        DisconnectSSH();
                    }
                })
                .setNegativeButton("Không", null)
                .show();

    }

    @Override 
	 protected void onPause() { 
	 // TODO Auto-generated method stub 
    	paused = true;
	 super.onPause(); 
	 //gọi hàm lưu trạng thái ở đây 
	 savingPreferences(); 
	 } 
	 @Override 
	 protected void onResume() { 
	 // TODO Auto-generated method stub 
		 paused = false;
	 super.onResume(); 
	 //gọi hàm đọc trạng thái ở đây 
	 restoringPreferences(); 
	 } 
	 public void savingPreferences() 
	 { 
	 //tạo đối tượng getSharedPreferences 
	 SharedPreferences pre=getSharedPreferences 
	 (prefname, MODE_PRIVATE); 
	 //tạo đối tượng Editor để lưu thay đổi 
	 SharedPreferences.Editor editor=pre.edit(); 
	 String luuip =edip.getText().toString(); 
	 String luuuse =eduse.getText().toString(); 
	 String luupass =edpass.getText().toString(); 

	 if(TTluu==true) 
	 { 
	 //xóa mọi lưu trữ trước đó 
	// editor.clear(); 
	// } 
	// else 
	// { 
	 //lưu vào editor 
	 editor.putString("luuip", luuip); 
	 editor.putString("luuuse", luuuse); 
	 editor.putString("luupass", luupass); 

	 TTluu=false;
	 } 
	 //chấp nhận lưu xuống file 
	 editor.commit(); 
	 } 
	 /** 
	 * hàm đọc trạng thái đã lưu trước đó 
	 */ 
	 @SuppressLint("NewApi") public void restoringPreferences() 
	 { 
	 SharedPreferences pre=getSharedPreferences 
	 (prefname,MODE_PRIVATE); 
	 String luuip=pre.getString("luuip", ""); 
	 String luuuse=pre.getString("luuuse", ""); 
	 String luupass=pre.getString("luupass", ""); 

	 edip.setText(luuip); 
	 eduse.setText(luuuse); 
	 edpass.setText(luupass); 
	 

	 } 
}
