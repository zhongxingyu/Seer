 /*
  * Copyright (C) 2011 microblinks UG (haftungsbeschraenkt)
  * See LICENSE.txt for licensing information.
  */
 
 package de.microblinks.l.core.device;
 
 import java.io.UnsupportedEncodingException;
 
 import javax.usb.UsbConst;
 import javax.usb.UsbControlIrp;
 import javax.usb.UsbDevice;
 import javax.usb.UsbDisconnectedException;
 import javax.usb.UsbException;
 import javax.usb.UsbInterface;
 import javax.usb.UsbInterfacePolicy;
 
 import de.microblinks.l.core.exceptions.LDeviceException;
 import de.microblinks.l.core.exceptions.LDisconnectedException;
 import de.microblinks.l.core.exceptions.LException;
 
 
 /**
  * The L device.
  *
  * @author Klaus Reimer (green@microblinks.de)
  */
 
 public final class LDevice
 {
     /** The vendor ID. */
     public static final int VENDOR_ID = 0x16c0;
 
     /** The product ID. */
     public static final int PRODUCT_ID = 0x5dc;
 
     /** The product ID while in bootloader. */
     public static final int BOOTLOADER_PRODUCT_ID = 0x5df;
 
     /** USB request to read data from the device. */
     public static final int REQ_READ_STATUS = 0x00;
 
     /** Bootloader USB request to read a page chunk from the flash. */
     public static final int REQ_GET_PAGE_WORD = 0xf9;
 
     /** Bootloader USB request to write a page to the flash. */
     public static final int REQ_WRITE_PAGE = 0xfa;
 
     /** Bootloader USB request to set a word in a page. */
     public static final int REQ_SET_PAGE_WORD = 0xfb;
 
     /** Bootloader USB request to erase a page. */
     public static final int REQ_ERASE_PAGE = 0xfc;
 
     /** Bootloader USB request to leave the bootloader. */
     public static final int REQ_LEAVE_BOOTLOADER = 0xfd;
 
     /** Bootloader USB request to read the flash size. */
     public static final int REQ_GET_FLASH_SIZE = 0xfe;
 
     /** Bootloader USB request to read the page size. */
     public static final int REQ_GET_PAGE_SIZE = 0xff;
 
     /** The USB device. */
     private final UsbDevice device;
 
     /** If device is currently in bootloader mode or not. */
     private Boolean bootloading;
 
     /** The USB interface policy. We always try to force-claim. */
     private static final UsbInterfacePolicy POLICY = new UsbInterfacePolicy()
     {
         @Override
         public boolean forceClaim(final UsbInterface usbInterface)
         {
             return true;
         }
     };
 
 
     /**
      * Constructor.
      *
      * @param device
      *            The USB device.
      */
 
     public LDevice(final UsbDevice device)
     {
         this.device = device;
     }
 
 
     /**
      * Returns the USB device.
      *
      * @return The USB device
      */
 
     UsbDevice getUsbDevice()
     {
         return this.device;
     }
 
 
     /**
      * Returns the serial number of the device.
      *
      * @return The serial number.
      * @throws LDisconnectedException
      *             When device is disconnected.
      * @throws LDeviceException
      *             When an error occurred.
      */
 
     public String getSerialNumber()
     {
         try
         {
             return this.device.getSerialNumberString();
         }
         catch (final UnsupportedEncodingException e)
         {
             throw new LDeviceException(e.getMessage(), e);
         }
         catch (final UsbDisconnectedException e)
         {
             throw new LDisconnectedException(e.getMessage(), e);
         }
         catch (final UsbException e)
         {
             throw new LDeviceException(e.getMessage(), e);
         }
     }
 
 
     /**
      * Returns the firmware version.
      *
      * @return The firmware version.
      */
 
     public LFirmwareVersion getFirmwareVersion()
     {
         return new LFirmwareVersion(this.device.getUsbDeviceDescriptor()
             .bcdDevice());
     }
 
 
     /**
      * Checks if device is currently in bootloader mode.
      *
      * @return True if device bootloading, false if not.
      */
 
     public boolean isBootloading()
     {
         if (this.bootloading == null)
         {
             this.bootloading =
                 this.device.getUsbDeviceDescriptor().idProduct() == BOOTLOADER_PRODUCT_ID
                     || (getFlags() & 0x80) != 0;
         }
         return this.bootloading;
     }
 
 
     /**
      * Creates a new request.
      *
      * @param requestType
      *            The request type.
      * @param request
      *            The request.
      * @param value
      *            The value.
      * @param index
      *            The index.
      * @return The request.
      */
 
     private UsbControlIrp createIrp(final int requestType,
         final int request, final int value, final int index)
     {
         return this.device.createUsbControlIrp((byte) requestType,
             (byte) request, (short) value, (short) index);
     }
 
 
     /**
      * Submits a requests.
      *
      * @param irp
      *            The request to submit
      * @throws LDisconnectedException
      *             When tried to communicate with disconnected device.
      * @throws LDeviceException
      *             When communication with the device fails.
      */
 
     private void submit(final UsbControlIrp irp) throws LDisconnectedException,
         LDeviceException
     {
         try
         {
             this.device.syncSubmit(irp);
         }
         catch (final UsbDisconnectedException e)
         {
             throw new LDisconnectedException(e.getMessage(), e);
         }
         catch (final UsbException e)
         {
             throw new LDeviceException(e.getMessage(), e);
         }
     }
 
 
     /**
      * Returns data from the device.
      *
      * @param index
      *            The start index of the data to read.
      * @param size
      *            The number of bytes to read.
      * @return The read data.
      */
 
     private byte[] readData(final int index, final int size)
     {
         final UsbControlIrp irp = createIrp(UsbConst.REQUESTTYPE_TYPE_VENDOR
             | UsbConst.REQUESTTYPE_RECIPIENT_DEVICE
             | UsbConst.ENDPOINT_DIRECTION_IN, 0, 0, index);
         final byte[] data = new byte[size];
         irp.setData(data);
         submit(irp);
         return data;
     }
 
 
     /**
      * Writes data to the device.
      *
      * @param index
      *            The starting index to write the data to.
      * @param data
      *            The data to write.
      */
 
     private void writeData(final int index, final byte[] data)
     {
         final UsbControlIrp irp = createIrp(UsbConst.REQUESTTYPE_TYPE_VENDOR
             | UsbConst.REQUESTTYPE_RECIPIENT_DEVICE
             | UsbConst.ENDPOINT_DIRECTION_OUT, 0, 0, index);
         irp.setData(data);
         submit(irp);
     }
 
 
     /**
      * Returns the number of procs.
      *
      * @return The number of procs
      */
 
     public int countProcs()
     {
         checkFirmware();
         return readData(1, 1)[0] & 0xff;
     }
 
 
     /**
      * Returns the number of variables (Including input and output variables).
      *
      * @return The number of variables.
      */
 
     public int countVars()
     {
         checkFirmware();
         final int procs = countProcs();
         return readData(procs * 6 + 2, 1)[0] & 0xff;
     }
 
 
     /**
      * Returns the number of output variables.
      *
      * @return The number of output variables.
      */
 
     public int countOutputs()
     {
         checkFirmware();
         final int procs = countProcs();
         return readData(procs * 6 + 3, 1)[0] & 0xff;
     }
 
 
     /**
      * Returns the number of input variables.
      *
      * @return The number of input variables.
      */
 
     public int countInputs()
     {
         checkFirmware();
         final int procs = countProcs();
         return readData(procs * 6 + 4, 1)[0] & 0xff;
     }
 
 
     /**
      * Sets an output variable value.
      *
      * @param index
      *            The output variable index.
      * @param value
      *            The output variable value.
      */
 
     public void setOutput(final int index, final int value)
     {
         checkFirmware();
         final int outputs = countOutputs();
         if (index >= outputs)
             throw new IllegalArgumentException(
                 "Output index must be lower than "
                     + outputs);
         final int procs = countProcs();
         writeData(6 + procs * 6 + (index & 0xff) * 2,
             new byte[] { (byte) value });
     }
 
 
     /**
      * Returns an output variable value.
      *
      * @param index
      *            The output variable index.
      * @return The output variable value.
      */
 
     public int getOutput(final int index)
     {
         checkFirmware();
         final int outputs = countOutputs();
         if (index >= outputs)
             throw new IllegalArgumentException(
                 "Output index must be lower than "
                     + outputs);
         final int procs = countProcs();
         return readData(6 + procs * 6 + (index & 0xff) * 2, 1)[0] & 0xff;
     }
 
 
     /**
      * Sets an input variable value.
      *
      * @param index
      *            The input variable index.
      * @param value
      *            The input variable value.
      */
 
     public void setInput(final int index, final int value)
     {
         checkFirmware();
         final int inputs = countInputs();
         if (index >= inputs)
             throw new IllegalArgumentException(
                 "Input index must be lower than "
                     + inputs);
         final int procs = countProcs();
         final int outputs = countOutputs();
         writeData(6 + procs * 6 + outputs * 2 + (index & 0xff) * 2,
             new byte[] { (byte) value });
     }
 
 
     /**
      * Returns an input variable value.
      *
      * @param index
      *            The input variable index.
      * @return The input variable value.
      */
 
     public int getInput(final int index)
     {
         checkFirmware();
         final int inputs = countInputs();
         if (index >= inputs)
             throw new IllegalArgumentException(
                 "Input index must be lower than "
                     + inputs);
         final int procs = countProcs();
         final int outputs = countOutputs();
         return readData(6 + procs * 6 + outputs * 2 + (index & 0xff) * 2,
             1)[0] & 0xff;
     }
 
 
     /**
      * Sets input variable values.
      *
      * @param values
      *            The input variables value to set.
      */
 
     public void setInputs(final int[] values)
     {
         checkFirmware();
         final int inputs = countInputs();
         if (values.length > inputs)
             throw new IllegalArgumentException(
                 "Number of input values must be lower than " + inputs);
         final int procs = countProcs();
         final int outputs = countOutputs();
         for (int i = 0; i < values.length; i++)
         {
             writeData(6 + procs * 6 + outputs * 2 + i * 2,
                 new byte[] { (byte) values[i] });
         }
     }
 
 
     /**
      * Returns the values of all input variables.
      *
      * @return The input variable values.
      */
 
     public int[] getInputs()
     {
         checkFirmware();
         final int inputs = countInputs();
         final int[] values = new int[inputs];
         final int procs = countProcs();
         final int outputs = countOutputs();
         for (int i = values.length - 1; i >= 0; i--)
         {
             values[i] = readData(6 + procs * 6 + outputs * 2 + i * 2, 1)[0]
                 & 0xff;
         }
         return values;
     }
 
 
     /**
      * Sets output variable values.
      *
      * @param values
      *            The output variables value to set.
      */
 
     public void setOutputs(final int[] values)
     {
         checkFirmware();
         final int outputs = countOutputs();
         if (values.length > outputs)
             throw new IllegalArgumentException(
                 "Number of output values must be lower than " + outputs);
         final int procs = countProcs();
         for (int i = 0; i < values.length; i++)
         {
             writeData(6 + procs * 6 + i * 2, new byte[] { (byte) values[i] });
         }
     }
 
 
     /**
      * Returns the values of all output variables.
      *
      * @return The output variable values.
      */
 
     public int[] getOutputs()
     {
         checkFirmware();
         final int outputs = countOutputs();
         final int[] values = new int[outputs];
         final int procs = countProcs();
         for (int i = values.length - 1; i >= 0; i--)
         {
             values[i] = readData(6 + procs * 6 + i * 2, 1)[0]
                 & 0xff;
         }
         return values;
     }
 
 
     /**
      * Saves the output values so the current output values are used as initial
      * output values when script is reset.
      */
 
     public void saveOutputs()
     {
         checkFirmware();
 
         final int outputs = countOutputs();
         final int procs = countProcs();
 
         // Read variable values
         final byte[] values = readData(5 + procs * 6, outputs * 2);
 
         // Copy current output values to initial input values
         for (int i = 0; i < outputs * 2; i += 2)
             values[i] = values[i + 1];
 
         // Write values back to device
         writeData(5 + procs * 6, values);
     }
 
 
     /**
      * Saves the input values so the current input values are used as initial
      * input values when script is reset.
      */
 
     public void saveInputs()
     {
         checkFirmware();
 
         final int outputs = countOutputs();
         final int inputs = countInputs();
         final int procs = countProcs();
 
         // Read variable values
         final byte[] values = readData(5 + procs * 6 + outputs * 2, inputs * 2);
 
         // Copy current input values to initial input values
         for (int i = 0; i < inputs * 2; i += 2)
             values[i] = values[i + 1];
 
         // Write values back to device
         writeData(5 + procs * 6 + outputs * 2, values);
     }
 
 
     /**
      * Returns the flags.
      *
      * @return The flags.
      */
 
     private int getFlags()
     {
         return readData(0, 1)[0] & 0xff;
     }
 
 
     /**
      * Sets Flags.
      *
      * @param andMask
      *            The AND mask.
      * @param orMask
      *            The OR mask.
      */
 
     private void setFlags(final int andMask, final int orMask)
     {
         final int flag = (getFlags() & andMask) | orMask;
         writeData(0, new byte[] { (byte) flag });
     }
 
 
     /**
      * Start the program.
      */
 
     public void start()
     {
         checkFirmware();
         setFlags(0xfe, 0x01);
     }
 
 
     /**
      * Stop the program.
      */
 
     public void stop()
     {
         checkFirmware();
         setFlags(0xfe, 0x00);
     }
 
 
     /**
      * Checks if program is running.
      *
      * @return True if program is running, false if not.
      */
 
     public boolean isRunning()
     {
         checkFirmware();
         return (getFlags() & 1) == 1;
     }
 
 
     /**
      * Returns the maximal program size.
      *
      * @return The maximal program size.
      */
 
     public int getMaxSize()
     {
         checkFirmware();
         final UsbControlIrp irp = createIrp(UsbConst.REQUESTTYPE_TYPE_VENDOR
                 | UsbConst.REQUESTTYPE_RECIPIENT_DEVICE
                 | UsbConst.ENDPOINT_DIRECTION_IN, 3, 0, 0);
         final byte[] data = new byte[2];
         irp.setData(data);
         submit(irp);
         return ((data[0] & 0xff) | ((data[1] & 0xff) << 8));
     }
 
 
     /**
      * Returns the size of the current program.
      *
      * @return The current program size.
      */
 
     public int getSize()
     {
         checkFirmware();
         final int procs = countProcs();
         final int vars = countVars();
         final byte[] data = readData(5 + procs * 6 + vars * 2, 2);
         final int codeSize = ((data[0] & 0xff) | ((data[1] & 0xff) << 8));
         return 6 + codeSize + procs * 6 + vars * 2;
     }
 
 
     /**
      * Returns the program.
      *
      * @return The program
      */
 
     public byte[] getProgram()
     {
         checkFirmware();
         final int size = getSize();
         final byte[] program = readData(1, size);
         return program;
     }
 
 
     /**
      * Sets the program.
      *
      * @param program
      *            The program to set.
      */
 
     public void setProgram(final byte[] program)
     {
         checkFirmware();
         writeData(1, program);
     }
 
 
     /**
      * Ensures that device is currently running the firmware and not the
      * bootloader.
      *
      * @throws IllegalStateException
      *             When device is currently in bootloader
      */
 
     private void checkFirmware()
     {
         if (isBootloading())
             throw new IllegalStateException("Device is waiting for firmware");
     }
 
 
     /**
      * Ensures that device is currently running the bootloader and not the
      * firmware.
      *
      * @throws IllegalStateException
      *             When device is currently in firemware
      */
 
     private void checkBootloader()
     {
         if (!isBootloading())
             throw new IllegalStateException("Device is not in bootloader");
     }
 
 
     /**
      * Saves the current program state to EEPROM.
      */
 
     public void saveState()
     {
         checkFirmware();
         final UsbControlIrp irp = createIrp(UsbConst.REQUESTTYPE_TYPE_VENDOR
                 | UsbConst.REQUESTTYPE_RECIPIENT_DEVICE
                 | UsbConst.ENDPOINT_DIRECTION_OUT, 1, 0, 0);
         submit(irp);
     }
 
 
     /**
      * Enters the boot loader.
      */
 
     public void enterBootloader()
     {
         if (isBootloading())
             throw new IllegalStateException("Bootloader already running");
         final UsbControlIrp irp = createIrp(UsbConst.REQUESTTYPE_TYPE_VENDOR
             | UsbConst.REQUESTTYPE_RECIPIENT_DEVICE
             | UsbConst.ENDPOINT_DIRECTION_OUT, 2, 0, 0);
         submit(irp);
     }
 
 
     /**
      * Claims the HID interface.
      *
      * @return The claimed interface.
      */
 
     private UsbInterface claim()
     {
         final UsbInterface iface =
             this.device.getUsbConfiguration((byte) 1).getUsbInterface((byte) 0);
         try
         {
             iface.claim(POLICY);
             return iface;
         }
         catch (final UsbDisconnectedException e)
         {
             throw new LDisconnectedException(e.getMessage(), e);
         }
         catch (final UsbException e)
         {
             throw new LDeviceException(e.getMessage(), e);
         }
     }
 
 
     /**
      * Releases the HID interface.
      *
      * @param iface
      *            The interface to release.
      */
 
     private void release(final UsbInterface iface)
     {
         try
         {
             iface.release();
         }
         catch (final UsbDisconnectedException e)
         {
             throw new LDisconnectedException(e.getMessage(), e);
         }
         catch (final UsbException e)
         {
             throw new LDeviceException(e.getMessage(), e);
         }
     }
 
 
     /**
      * Checks if boot loader is version 1.
      *
      * @return True if boot loader is version 1, 0 if not.
      */
 
     private boolean isBootloaderV1()
     {
         return this.device.getUsbDeviceDescriptor().bcdDevice() < 0x200;
     }
 
 
     /**
      * Leaves the boot loader.
      */
 
     public void leaveBootloader()
     {
         checkBootloader();
 
         if (isBootloaderV1())
         {
             leaveBootloaderV1();
             return;
         }
 
         final UsbControlIrp irp =
             createIrp(UsbConst.REQUESTTYPE_TYPE_VENDOR
                 | UsbConst.REQUESTTYPE_RECIPIENT_DEVICE
                 | UsbConst.ENDPOINT_DIRECTION_OUT, REQ_LEAVE_BOOTLOADER, 0,
                 0);
         submit(irp);
     }
 
 
     /**
      * Only for Bootloader V1. Leaves the boot loader.
      */
 
     private void leaveBootloaderV1()
     {
         final UsbInterface iface = claim();
         try
         {
             final UsbControlIrp irp =
                     createIrp(UsbConst.REQUESTTYPE_TYPE_CLASS
                         | UsbConst.REQUESTTYPE_RECIPIENT_INTERFACE
                         | UsbConst.ENDPOINT_DIRECTION_OUT,
                         9, 0x0300, 0);
             final byte[] data = { 1 };
             irp.setData(data);
             submit(irp);
         }
         finally
         {
             release(iface);
         }
     }
 
 
     /**
      * Returns the pre-scaling index.
      *
      * @return The pre-scaling index.
      */
 
     public int getPrescalingIndex()
     {
         return (getFlags() & 0x1c) >> 2;
     }
 
 
     /**
      * Sets the pre-scaling index.
      *
      * @param index
      *            The pre-scaling index to set.
      */
 
     public void setPrescalingIndex(final int index)
     {
         if (index < 0 || index > 4)
             throw new IllegalArgumentException(
                 "Pre-scaling index must be between 0 and 4");
         setFlags(0xe3, (index & 7) << 2);
     }
 
 
     /**
      * Checks if outputs are inverted.
      *
      * @return True if outputs are inverted, false if not.
      */
 
     public boolean isInverted()
     {
         return (getFlags() & 0x02) != 0;
     }
 
 
     /**
      * Enables or disabled output inversion.
      *
      * @param inverted
      *            True to invert outputs, false to not invert them.
      */
 
     public void setInverted(final boolean inverted)
     {
         setFlags(0xfd, inverted ? 2 : 0);
     }
 
 
     /**
      * Returns the page size of the flash memory.
      *
      * @return The page size of the flash memory.
      */
 
     public int getPageSize()
     {
         checkBootloader();
         final UsbControlIrp irp = createIrp(UsbConst.REQUESTTYPE_TYPE_VENDOR
             | UsbConst.REQUESTTYPE_RECIPIENT_DEVICE
             | UsbConst.ENDPOINT_DIRECTION_IN, REQ_GET_PAGE_SIZE, 0, 0);
         irp.setData(new byte[2]);
         submit(irp);
         final byte[] data = irp.getData();
         return (data[0] & 0xff) | ((data[1] & 0xff) << 8);
     }
 
 
     /**
      * Returns the size of the flash memory.
      *
      * @return The size of the flash memory.
      */
 
     public int getFlashSize()
     {
         checkBootloader();
         final UsbControlIrp irp = createIrp(UsbConst.REQUESTTYPE_TYPE_VENDOR
             | UsbConst.REQUESTTYPE_RECIPIENT_DEVICE
             | UsbConst.ENDPOINT_DIRECTION_IN, REQ_GET_FLASH_SIZE, 0, 0);
         irp.setData(new byte[2]);
         submit(irp);
         final byte[] data = irp.getData();
         return (data[0] & 0xff) | ((data[1] & 0xff) << 8);
     }
 
 
     /**
      * Writes the specified firmware to the device.
      *
      * @param firmware
      *            The firmware to write
      * @param listener
      *            Optional listener to get progress information.
      */
 
     public void writeFirmware(final byte[] firmware,
         final LFlashProgressListener listener)
     {
         checkBootloader();
 
         if (isBootloaderV1())
         {
             writeFirmwareV1(firmware, listener);
             return;
         }
 
         final int size = firmware.length;
         final LFlashProgressEvent event = new LFlashProgressEvent(this, size);
         if (listener != null) listener.progressStart(event);
 
         final int pageSize = getPageSize();
         final int flashSize = getFlashSize();
 
         // Check if flash fits into device
         if (flashSize < size)
            throw new LDeviceException(String.format(
                "Firmware (%db) doesn't fit into device (%db).", size,
                flashSize));
 
         final int pages = (size + pageSize - 1) / pageSize;
         for (int page = 0; page < pages; page++)
         {
             final int bufferSize = Math.min(pageSize, size - page * 64);
 
             // Clear page
             UsbControlIrp irp = createIrp(UsbConst.REQUESTTYPE_TYPE_VENDOR
                 | UsbConst.REQUESTTYPE_RECIPIENT_DEVICE
                 | UsbConst.ENDPOINT_DIRECTION_OUT, REQ_ERASE_PAGE, 0,
                 page * pageSize);
             submit(irp);
 
             // Fill page
             for (int i = 0; i < bufferSize - 1; i += 2)
             {
                 // Write page
                 irp = createIrp(UsbConst.REQUESTTYPE_TYPE_VENDOR
                     | UsbConst.REQUESTTYPE_RECIPIENT_DEVICE
                     | UsbConst.ENDPOINT_DIRECTION_OUT, REQ_SET_PAGE_WORD,
                     (firmware[page * pageSize + i] & 0xff) |
                         ((firmware[page * pageSize + i + 1] & 0xff) << 8)
                     , page * pageSize + i);
                 submit(irp);
                 event.incrementTransferred(2);
                 if (listener != null) listener.progressUpdate(event);
             }
 
             // Write page
             irp = createIrp(UsbConst.REQUESTTYPE_TYPE_VENDOR
                 | UsbConst.REQUESTTYPE_RECIPIENT_DEVICE
                 | UsbConst.ENDPOINT_DIRECTION_OUT, REQ_WRITE_PAGE, 0,
                 page * pageSize);
             submit(irp);
         }
 
         if (listener != null) listener.progressFinish(event);
     }
 
 
     /**
      * Only for boot loader v1. Writes the specified firmware to the device.
      *
      * @param firmware
      *            The firmware to write
      * @param listener
      *            Optional listener to get progress information.
      */
 
     private void writeFirmwareV1(final byte[] firmware,
         final LFlashProgressListener listener)
     {
         checkBootloader();
 
         final int size = firmware.length;
         final LFlashProgressEvent event = new LFlashProgressEvent(this, size);
         if (listener != null) listener.progressStart(event);
 
         final UsbInterface iface = claim();
         try
         {
             final byte[] data = new byte[132];
 
             // Read page and device size
             UsbControlIrp irp = createIrp(UsbConst.REQUESTTYPE_TYPE_CLASS
                 | UsbConst.REQUESTTYPE_RECIPIENT_INTERFACE
                 | UsbConst.ENDPOINT_DIRECTION_IN,
                 1, 0x0301, 0);
             irp.setData(data);
             submit(irp);
             final int pageSize = (data[2] & 0xff) << 8 | (data[1] & 0xff);
             final int deviceSize = (data[6] & 0xff) << 24
                 | (data[5] & 0xff) << 16
                 | (data[4] & 0xff) << 8
                 | (data[3] & 0xff);
 
             // Check if flash fits into device
             if (deviceSize < size + 2048)
                throw new LDeviceException(String.format(
                    "Firmware (%db) doesn't fit into device (%db).", size,
                    deviceSize - 2048));
 
             // Calculate starting and ending address
             int startAddr = 0;
             int endAddr = size;
             int mask;
             if (pageSize < 128)
                 mask = 127;
             else
                 mask = pageSize - 1;
             startAddr &= ~mask; // round down
             endAddr = (endAddr + mask) & ~mask; // round up
 
             // Write the firmware to the device
             while (startAddr < endAddr)
             {
                 data[0] = 2;
                 data[1] = (byte) ((startAddr & 0xff));
                 data[2] = (byte) (((startAddr) >>> 8) & 0xff);
                 data[3] = (byte) (((startAddr) >>> 16) & 0xff);
                 final int blockSize = Math.min(size - startAddr, 128);
                 System.arraycopy(firmware, startAddr, data, 4, blockSize);
                 event.setTransferred(startAddr + blockSize);
                 if (listener != null) listener.progressUpdate(event);
                 irp = createIrp(UsbConst.REQUESTTYPE_TYPE_CLASS
                     | UsbConst.REQUESTTYPE_RECIPIENT_INTERFACE
                     | UsbConst.ENDPOINT_DIRECTION_OUT,
                     9, 0x0302, 0);
                 irp.setData(data);
                 submit(irp);
                 startAddr += 128;
             }
         }
         finally
         {
             release(iface);
         }
 
         if (listener != null) listener.progressFinish(event);
     }
 
 
     /**
      * Reads current firmware from the device.
      *
      * @param listener
      *            Optional listener to get progress information.
      * @return The read firmware
      */
 
     public byte[] readFirmware(final LFlashProgressListener listener)
     {
         checkBootloader();
 
         if (isBootloaderV1())
             throw new LDeviceException("Bootloader too old for this command");
 
         final int flashSize = getFlashSize();
         final LFlashProgressEvent event =
             new LFlashProgressEvent(this, flashSize);
         if (listener != null) listener.progressStart(event);
         final byte[] firmware = new byte[flashSize];
 
         for (int i = 0; i < flashSize; i += 2)
         {
             // Clear page
             final UsbControlIrp irp =
                 createIrp(UsbConst.REQUESTTYPE_TYPE_VENDOR
                     | UsbConst.REQUESTTYPE_RECIPIENT_DEVICE
                     | UsbConst.ENDPOINT_DIRECTION_IN, REQ_GET_PAGE_WORD, 0,
                     i);
             // irp.setData(new byte[2]);
             irp.setData(firmware, i, 2);
             submit(irp);
             event.incrementTransferred(2);
             if (listener != null) listener.progressUpdate(event);
         }
 
         if (listener != null) listener.progressFinish(event);
 
         return firmware;
     }
 
 
     /**
      * Looks for a device with the specified serial number and the specified
      * bootloading mode.
      *
      * @param serial
      *            The serial number.
      * @param bootloading
      *            The bootloading mode.
      * @return The found device or null if not found.
      */
 
     private LDevice findDevice(final String serial, final boolean bootloading)
     {
         try
         {
             final LDevice device =
                 LDeviceManager.getInstance().getDevice(serial);
             if (device == null) return null;
             this.bootloading = null;
             if (device.isBootloading() != bootloading) return null;
             return device;
         }
         catch (final LException e)
         {
             return null;
         }
     }
 
 
     /**
      * Sleeps for the specified number of milliseconds.
      *
      * @param millis
      *            The time to sleep.
      */
 
     private void sleep(final long millis)
     {
         try
         {
             Thread.sleep(millis);
         }
         catch (final InterruptedException e)
         {
             Thread.currentThread().interrupt();
         }
     }
 
 
     /**
      * Flashes the device with the specified firmware and returns the newly
      * flashed device if successful.
      *
      * @param firmware
      *            The firmware to flash.
      * @param listener
      *            The firmware flash progress listener.
      * @return The device after flashing.
      */
 
     public LDevice flash(final byte[] firmware,
         final LFlashProgressListener listener)
     {
         LDevice device = this;
 
         // Remember serial number
         final String serial = getSerialNumber();
 
         // If necessary start bootloader and wait until it is started
         if (!isBootloading())
         {
             enterBootloader();
             sleep(250);
             int tries = 20;
             while ((device = findDevice(serial, true)) == null)
             {
                 sleep(250);
                 tries--;
                 if (tries == 0)
                     throw new LDeviceException(
                         "Unable to find device after starting bootloader");
             }
         }
 
         // Write the firmware
         device.writeFirmware(firmware, listener);
 
         // Leave bootloader and wait until firmware is loaded
         device.leaveBootloader();
         sleep(250);
         int tries = 20;
         while ((device = findDevice(serial, false)) == null)
         {
             sleep(250);
             tries--;
             if (tries == 0)
                 throw new LDeviceException(
                     "Unable to find device after starting firmware");
         }
 
         // Return the new device
         return device;
     }
 }
