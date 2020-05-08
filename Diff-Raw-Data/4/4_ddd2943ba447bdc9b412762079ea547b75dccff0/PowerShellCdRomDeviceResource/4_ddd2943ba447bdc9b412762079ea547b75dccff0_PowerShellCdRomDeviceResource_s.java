 package com.redhat.rhevm.api.powershell.resource;
 
 import com.redhat.rhevm.api.model.CdRom;
 import com.redhat.rhevm.api.model.CdRoms;
 
public class PowerShellCdRomDeviceResource extends PowerShellDeviceResource<CdRom, CdRoms> {
 
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
