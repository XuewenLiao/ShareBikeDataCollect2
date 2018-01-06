package com.hnulab.sharebike.em.broadcast;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.hnulab.sharebike.em.util.BluetoothAutoConnectUtils;


/**
 * description:蓝牙连接广播
 * auther：luojie
 * time：2017/9/12 16:27
 */

public class BluetoothReceiver extends BroadcastReceiver {

          public static String BLUETOOTH_PIN = "";  //此处为你要连接的蓝牙设备的初始密钥，一般为1234或0000
          public static String BLUETOOTH_ADDRESS = "";  //此处为你要连接的蓝牙设备的初始密钥，一般为1234或0000
          public static String EXTRA_DEVICE_ADDRESS = "address";

          //广播action
          private String ACTION_UPDATEUI = "com.hnulab.sharebike.update";

          public BluetoothReceiver() {

          }

          //广播接收器，当远程蓝牙设备被发现时，回调函数onReceiver()会被执行
          @Override
          public void onReceive(Context context, Intent intent) {

                    String action = intent.getAction(); //得到action
                    Log.e("action1=", action);
                    BluetoothDevice btDevice = null;  //创建一个蓝牙device对象
                    // 从Intent中获取设备对象
                    btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                              Log.e("发现设备:", "[" + btDevice.getName() + "]" + ":" + btDevice.getAddress());
                              //HC-05设备如果有多个，第一个搜到的那个会被尝试。
                              if (btDevice.getAddress().contains(BLUETOOTH_ADDRESS)) {
                                        if (btDevice.getBondState() == BluetoothDevice.BOND_NONE) {

                                                  Log.e("ywq", "attemp to bond:" + "[" + btDevice.getName() + "]");
                                                  try {
                                                            //通过工具类BlueToothAutoConnectUtils,调用createBond方法
                                                            BluetoothAutoConnectUtils.createBond(btDevice.getClass(), btDevice);
                                                  } catch (Exception e) {
                                                            // TODO Auto-generated catch block
                                                            e.printStackTrace();
                                                  }
                                        }
                              } else
                                        Log.e("error", "Is faild");
                              //再次得到的action，会等于PAIRING_REQUEST
                    } else if (action.equals("android.bluetooth.device.action.PAIRING_REQUEST")) {
                              Log.e("action2=", action);
                              if (btDevice.getAddress().contains(BLUETOOTH_ADDRESS)) {
                                        Log.e("here", "OKOKOK");

                                        try {

                                                  //1.确认配对
//					BluetoothAutoConnectUtils.setPairingConfirmation(btDevice.getClass(), btDevice, true);
                                                  //2.终止有序广播
                                                  Log.i("order...", "isOrderedBroadcast:" + isOrderedBroadcast() + ",isInitialStickyBroadcast:" + isInitialStickyBroadcast());
                                                  abortBroadcast();
                                                  //如果没有将广播终止，则会出现一个一闪而过的配对框。
                                                  //3.调用setPin方法进行配对...
                                                  boolean ret = BluetoothAutoConnectUtils.setPin(btDevice.getClass(), btDevice, BLUETOOTH_PIN);
                                                  //配对成功，通知UI进行更新
                                                  if (ret) {
                                                            Intent intent2 = new Intent();
                                                            intent2.setAction(ACTION_UPDATEUI);
                                                            intent2.putExtra(EXTRA_DEVICE_ADDRESS, btDevice.getAddress());
                                                            context.sendBroadcast(intent2);
                                                  }

                                        } catch (Exception e) {
                                                  // TODO Auto-generated catch block
                                                  e.printStackTrace();
                                        }
                              } else
                                        Log.e("提示信息", "这个设备不是目标蓝牙设备");
                    }
          }
}