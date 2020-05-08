 /*
 * This file is part of LinkJVM.
 *
 * Java Framework for the KIPR Link
 * Copyright (C) 2013 Markus Klein<m@mklein.co.at>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
 
 /*
  * IMPORTANT!!!
  * Do not make changes to this file unless you know what you are doing--modify
  * the SWIG interface file instead.
  */
 
 package libkovan.jni;
 
 /**
  * This class provides the pure java implementation of the libkovan.
  * It is used by the higher wrapper classes and should not be used in another way.
  * 
  * @author Markus Klein
  * @version 1.0.0
  *
  */
 
 public class LinkJVM {
   public static void wait_for_light(int light_port_) {
     LinkJVMJNI.wait_for_light(light_port_);
   }
 
   public static void shut_down_in(double delay) {
     LinkJVMJNI.shut_down_in(delay);
   }
 
   public static void beep() {
     LinkJVMJNI.beep__SWIG_0();
   }
 
   public static int get_motor_position_counter(int motor) {
     return LinkJVMJNI.get_motor_position_counter(motor);
   }
 
   public static void clear_motor_position_counter(int motor) {
     LinkJVMJNI.clear_motor_position_counter(motor);
   }
 
   public static int move_at_velocity(int motor, int velocity) {
     return LinkJVMJNI.move_at_velocity(motor, velocity);
   }
 
   public static int mav(int motor, int velocity) {
     return LinkJVMJNI.mav(motor, velocity);
   }
 
   public static int move_to_position(int motor, int speed, int goal_pos) {
     return LinkJVMJNI.move_to_position(motor, speed, goal_pos);
   }
 
   public static int mtp(int motor, int speed, int goal_pos) {
     return LinkJVMJNI.mtp(motor, speed, goal_pos);
   }
 
   public static int move_relative_position(int motor, int speed, int delta_pos) {
     return LinkJVMJNI.move_relative_position(motor, speed, delta_pos);
   }
 
   public static int mrp(int motor, int speed, int delta_pos) {
     return LinkJVMJNI.mrp(motor, speed, delta_pos);
   }
 
   public static void set_pid_gains(int motor, short p, short i, short d, short pd, short id, short dd) {
     LinkJVMJNI.set_pid_gains(motor, p, i, d, pd, id, dd);
   }
 
   public static void get_pid_gains(int motor, SWIGTYPE_p_short p, SWIGTYPE_p_short i, SWIGTYPE_p_short d, SWIGTYPE_p_short pd, SWIGTYPE_p_short id, SWIGTYPE_p_short dd) {
     LinkJVMJNI.get_pid_gains(motor, SWIGTYPE_p_short.getCPtr(p), SWIGTYPE_p_short.getCPtr(i), SWIGTYPE_p_short.getCPtr(d), SWIGTYPE_p_short.getCPtr(pd), SWIGTYPE_p_short.getCPtr(id), SWIGTYPE_p_short.getCPtr(dd));
   }
 
   public static int freeze(int motor) {
     return LinkJVMJNI.freeze(motor);
   }
 
   public static int get_motor_done(int motor) {
     return LinkJVMJNI.get_motor_done(motor);
   }
 
   public static void block_motor_done(int motor) {
     LinkJVMJNI.block_motor_done(motor);
   }
 
   public static void bmd(int motor) {
     LinkJVMJNI.bmd(motor);
   }
 
   public static int setpwm(int motor, int pwm) {
     return LinkJVMJNI.setpwm(motor, pwm);
   }
 
   public static int getpwm(int motor) {
     return LinkJVMJNI.getpwm(motor);
   }
 
   public static void fd(int motor) {
     LinkJVMJNI.fd(motor);
   }
 
   public static void bk(int motor) {
     LinkJVMJNI.bk(motor);
   }
 
   public static void motor(int motor, int percent) {
     LinkJVMJNI.motor(motor, percent);
   }
 
   public static void off(int motor) {
     LinkJVMJNI.off(motor);
   }
 
   public static void alloff() {
     LinkJVMJNI.alloff();
   }
 
   public static void ao() {
     LinkJVMJNI.ao();
   }
 
   public static int create_connect() {
     return LinkJVMJNI.create_connect();
   }
 
   public static void create_disconnect() {
     LinkJVMJNI.create_disconnect();
   }
 
   public static void create_start() {
     LinkJVMJNI.create_start();
   }
 
   public static void create_passive() {
     LinkJVMJNI.create_passive();
   }
 
   public static void create_safe() {
     LinkJVMJNI.create_safe();
   }
 
   public static void create_full() {
     LinkJVMJNI.create_full();
   }
 
   public static void create_spot() {
     LinkJVMJNI.create_spot();
   }
 
   public static void create_cover() {
     LinkJVMJNI.create_cover();
   }
 
   public static void create_demo(int d) {
     LinkJVMJNI.create_demo(d);
   }
 
   public static void create_cover_dock() {
     LinkJVMJNI.create_cover_dock();
   }
 
   public static int get_create_mode() {
     return LinkJVMJNI.get_create_mode();
   }
 
   public static int get_create_lbump() {
     return LinkJVMJNI.get_create_lbump();
   }
 
   public static int get_create_rbump() {
     return LinkJVMJNI.get_create_rbump();
   }
 
   public static int get_create_lwdrop() {
     return LinkJVMJNI.get_create_lwdrop();
   }
 
   public static int get_create_cwdrop() {
     return LinkJVMJNI.get_create_cwdrop();
   }
 
   public static int get_create_rwdrop() {
     return LinkJVMJNI.get_create_rwdrop();
   }
 
   public static int get_create_wall() {
     return LinkJVMJNI.get_create_wall();
   }
 
   public static int get_create_lcliff() {
     return LinkJVMJNI.get_create_lcliff();
   }
 
   public static int get_create_lfcliff() {
     return LinkJVMJNI.get_create_lfcliff();
   }
 
   public static int get_create_rfcliff() {
     return LinkJVMJNI.get_create_rfcliff();
   }
 
   public static int get_create_rcliff() {
     return LinkJVMJNI.get_create_rcliff();
   }
 
   public static int get_create_vwall() {
     return LinkJVMJNI.get_create_vwall();
   }
 
   public static int get_create_overcurrents() {
     return LinkJVMJNI.get_create_overcurrents();
   }
 
   public static int get_create_infrared() {
     return LinkJVMJNI.get_create_infrared();
   }
 
   public static int get_create_advance_button() {
     return LinkJVMJNI.get_create_advance_button();
   }
 
   public static int get_create_play_button() {
     return LinkJVMJNI.get_create_play_button();
   }
 
   public static int get_create_normalized_angle() {
     return LinkJVMJNI.get_create_normalized_angle();
   }
 
   public static void set_create_normalized_angle(int angle) {
     LinkJVMJNI.set_create_normalized_angle(angle);
   }
 
   public static int get_create_total_angle() {
     return LinkJVMJNI.get_create_total_angle();
   }
 
   public static void set_create_total_angle(int angle) {
     LinkJVMJNI.set_create_total_angle(angle);
   }
 
   public static int get_create_distance() {
     return LinkJVMJNI.get_create_distance();
   }
 
   public static void set_create_distance(int dist) {
     LinkJVMJNI.set_create_distance(dist);
   }
 
   public static int get_create_battery_charging_state() {
     return LinkJVMJNI.get_create_battery_charging_state();
   }
 
   public static int get_create_battery_voltage() {
     return LinkJVMJNI.get_create_battery_voltage();
   }
 
   public static int get_create_battery_current() {
     return LinkJVMJNI.get_create_battery_current();
   }
 
   public static int get_create_battery_temp() {
     return LinkJVMJNI.get_create_battery_temp();
   }
 
   public static int get_create_battery_charge() {
     return LinkJVMJNI.get_create_battery_charge();
   }
 
   public static int get_create_battery_capacity() {
     return LinkJVMJNI.get_create_battery_capacity();
   }
 
   public static int get_create_wall_amt() {
     return LinkJVMJNI.get_create_wall_amt();
   }
 
   public static int get_create_lcliff_amt() {
     return LinkJVMJNI.get_create_lcliff_amt();
   }
 
   public static int get_create_lfcliff_amt() {
     return LinkJVMJNI.get_create_lfcliff_amt();
   }
 
   public static int get_create_rfcliff_amt() {
     return LinkJVMJNI.get_create_rfcliff_amt();
   }
 
   public static int get_create_rcliff_amt() {
     return LinkJVMJNI.get_create_rcliff_amt();
   }
 
   public static int get_create_bay_DI() {
     return LinkJVMJNI.get_create_bay_DI();
   }
 
   public static int get_create_bay_AI() {
     return LinkJVMJNI.get_create_bay_AI();
   }
 
   public static int get_create_song_number() {
     return LinkJVMJNI.get_create_song_number();
   }
 
   public static int get_create_song_playing() {
     return LinkJVMJNI.get_create_song_playing();
   }
 
   public static int get_create_number_of_stream_packets() {
     return LinkJVMJNI.get_create_number_of_stream_packets();
   }
 
   public static int get_create_requested_velocity() {
     return LinkJVMJNI.get_create_requested_velocity();
   }
 
   public static int get_create_requested_radius() {
     return LinkJVMJNI.get_create_requested_radius();
   }
 
   public static int get_create_requested__right_velocity() {
     return LinkJVMJNI.get_create_requested__right_velocity();
   }
 
   public static int get_create_requested_left_velocity() {
     return LinkJVMJNI.get_create_requested_left_velocity();
   }
 
   public static void create_stop() {
     LinkJVMJNI.create_stop();
   }
 
   public static void create_drive(int speed, int radius) {
     LinkJVMJNI.create_drive(speed, radius);
   }
 
   public static void create_drive_straight(int speed) {
     LinkJVMJNI.create_drive_straight(speed);
   }
 
   public static void create_spin_CW(int speed) {
     LinkJVMJNI.create_spin_CW(speed);
   }
 
   public static void create_spin_CCW(int speed) {
     LinkJVMJNI.create_spin_CCW(speed);
   }
 
   public static void create_drive_direct(int r_speed, int l_speed) {
     LinkJVMJNI.create_drive_direct(r_speed, l_speed);
   }
 
   public static void create_advance_led(int on) {
     LinkJVMJNI.create_advance_led(on);
   }
 
   public static void create_play_led(int on) {
     LinkJVMJNI.create_play_led(on);
   }
 
   public static void create_power_led(int color, int brightness) {
     LinkJVMJNI.create_power_led(color, brightness);
   }
 
   public static void create_load_song(int num) {
     LinkJVMJNI.create_load_song(num);
   }
 
   public static void create_play_song(int num) {
     LinkJVMJNI.create_play_song(num);
   }
 
   public static void set_digital_output(int port, int inout) {
     LinkJVMJNI.set_digital_output(port, inout);
   }
 
   public static void set_digital_value(int port, int value) {
     LinkJVMJNI.set_digital_value(port, value);
   }
 
   public static void display_clear() {
     LinkJVMJNI.display_clear();
   }
 
   public static int a_button() {
     return LinkJVMJNI.a_button();
   }
 
  public static int analog(int p) {
    return LinkJVMJNI.analog(p);
   }
 
   public static int analog10(int p) {
     return LinkJVMJNI.analog10(p);
   }
 
   public static int b_button() {
     return LinkJVMJNI.b_button();
   }
 
   public static int side_button() {
     return LinkJVMJNI.side_button();
   }
 
   public static int digital(int p) {
     return LinkJVMJNI.digital(p);
   }
 
   public static double power_level() {
     return LinkJVMJNI.power_level();
   }
 
   public static void set_each_analog_state(int a0, int a1, int a2, int a3, int a4, int a5, int a6, int a7) {
     LinkJVMJNI.set_each_analog_state(a0, a1, a2, a3, a4, a5, a6, a7);
   }
 
   public static void set_analog_pullup(int port, int pullupTF) {
     LinkJVMJNI.set_analog_pullup(port, pullupTF);
   }
 
   public static void disable_servos() {
     LinkJVMJNI.disable_servos();
   }
 
   public static void enable_servos() {
     LinkJVMJNI.enable_servos();
   }
 
   public static int get_servo_position(int srv) {
     return LinkJVMJNI.get_servo_position(srv);
   }
 
   public static void set_servo_position(int srv, int pos) {
     LinkJVMJNI.set_servo_position(srv, pos);
   }
 
   public static void disable_servo(int port) {
     LinkJVMJNI.disable_servo(port);
   }
 
   public static void enable_servo(int port) {
     LinkJVMJNI.enable_servo(port);
   }
 
   public static int get_servo_enabled(int port) {
     return LinkJVMJNI.get_servo_enabled(port);
   }
 
   public static void set_servo_enabled(int port, int enabled) {
     LinkJVMJNI.set_servo_enabled(port, enabled);
   }
 
   public static void msleep(int msec) {
     LinkJVMJNI.msleep(msec);
   }
 
   public static double seconds() {
     return LinkJVMJNI.seconds();
   }
 
   public static void camera_close() {
     LinkJVMJNI.camera_close();
   }
 
   public static int camera_load_config(String name) {
     return LinkJVMJNI.camera_load_config(name);
   }
 
   public static int camera_open(int res_numb) {
     return LinkJVMJNI.camera_open(res_numb);
   }
 
   public static int camera_update() {
     return LinkJVMJNI.camera_update();
   }
 
   public static int get_channel_count() {
     return LinkJVMJNI.get_channel_count();
   }
 
   public static int get_code_num(int channel, int object) {
     return LinkJVMJNI.get_code_num(channel, object);
   }
 
   public static int get_object_area(int channel, int object) {
     return LinkJVMJNI.get_object_area(channel, object);
   }
 
   public static double get_object_confidence(int channel, int object) {
     return LinkJVMJNI.get_object_confidence(channel, object);
   }
 
   public static int get_object_count(int channel) {
     return LinkJVMJNI.get_object_count(channel);
   }
 
   public static String get_object_data(int channel, int object) {
     return LinkJVMJNI.get_object_data(channel, object);
   }
 
   public static int get_object_data_length(int channel, int object) {
     return LinkJVMJNI.get_object_data_length(channel, object);
   }
 
 }
