 package com.redhat.rhevm.api.powershell.resource;
 
 import com.redhat.rhevm.api.model.CdRom;
 import com.redhat.rhevm.api.model.CdRoms;
import com.redhat.rhevm.api.resource.DeviceResource;
 
public class PowerShellCdRomDeviceResource extends PowerShellDeviceResource<CdRom, CdRoms> implements DeviceResource<CdRom> {
 
     public PowerShellCdRomDeviceResource(PowerShellCdRomsResource parent, String deviceId) {
         super(parent, deviceId);
     }
 
     @Override
     public CdRom update(CdRom cdrom) {
         return cdrom.isSetFile() && cdrom.getFile().isSetId()
                ? ((PowerShellCdRomsResource)parent).updateCdRom(cdrom.getFile().getId())
                : get();
     }
 }
