 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.porcupine.psp.model.entity;
 
 import com.porcupine.psp.model.vo.*;
 import java.io.Serializable;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import javax.persistence.*;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 
 /**
  *
  * @author Zergio
  */
 @Entity
 @Table(name = "IMPL_SEGURIDAD")
 @XmlRootElement
 @NamedQueries({
     @NamedQuery(name = "ImplSeguridad.findAll", query = "SELECT i FROM ImplSeguridad i"),
     @NamedQuery(name = "ImplSeguridad.findByIdImplemento", query = "SELECT i FROM ImplSeguridad i WHERE i.idImplemento = :idImplemento"),
     @NamedQuery(name = "ImplSeguridad.findByNombreI", query = "SELECT i FROM ImplSeguridad i WHERE i.nombreI = :nombreI"),
     @NamedQuery(name = "ImplSeguridad.findByPrecioUnitarioI", query = "SELECT i FROM ImplSeguridad i WHERE i.precioUnitarioI = :precioUnitarioI"),
     @NamedQuery(name = "ImplSeguridad.findByCantidad", query = "SELECT i FROM ImplSeguridad i WHERE i.cantidad = :cantidad"),
     @NamedQuery(name = "ImplSeguridad.findByDescripcionI", query = "SELECT i FROM ImplSeguridad i WHERE i.descripcionI = :descripcionI"),
     @NamedQuery(name = "ImplSeguridad.findByEstadoI", query = "SELECT i FROM ImplSeguridad i WHERE i.estadoI = :estadoI"),
     @NamedQuery(name = "ImplSeguridad.findByFechaRegIm", query = "SELECT i FROM ImplSeguridad i WHERE i.fechaRegIm = :fechaRegIm")})
 public class ImplSeguridad implements Serializable {
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Basic(optional = false)
     @Column(name = "ID_IMPLEMENTO")
     private Short idImplemento;
     @Basic(optional = false)
     @Column(name = "NOMBRE_I")
     private String nombreI;
     // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
     @Basic(optional = false)
     @Column(name = "PRECIO_UNITARIO_I")
     private BigDecimal precioUnitarioI;
     @Basic(optional = false)
     @Column(name = "CANTIDAD")
     private short cantidad;
     @Basic(optional = false)
     @Column(name = "DESCRIPCION_I")
     private String descripcionI;
     @Basic(optional = false)
     @Column(name = "ESTADO_I")
     private String estadoI;
     @Basic(optional = false)
     @Column(name = "FECHA_REG_IM")
     @Temporal(TemporalType.TIMESTAMP)
     private Date fechaRegIm;
     @JoinColumn(name = "ID_PRO", referencedColumnName = "ID_PRO")
     @ManyToOne
     private Proveedor idPro;
     @JoinColumn(name = "CEDULAE", referencedColumnName = "CEDULAE")
     @ManyToOne(optional = false)
     private CoordTYT cedulae;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "idImplemento")
     private List<AsigImpl> asigImplList;
     @OneToMany(mappedBy = "idImplemento")
     private List<ActuImpl> actuImplList;
 
     public ImplSeguridad() {
     }
 
     public ImplSeguridad(Short idImplemento) {
         this.idImplemento = idImplemento;
     }
 
     public ImplSeguridad(Short idImplemento, String nombreI, BigDecimal precioUnitarioI, short cantidad, String descripcionI, String estadoI, Date fechaRegIm) {
         this.idImplemento = idImplemento;
         this.nombreI = nombreI;
         this.precioUnitarioI = precioUnitarioI;
         this.cantidad = cantidad;
         this.descripcionI = descripcionI;
         this.estadoI = estadoI;
         this.fechaRegIm = fechaRegIm;
     }
 
     public Short getIdImplemento() {
         return idImplemento;
     }
 
     public void setIdImplemento(Short idImplemento) {
         this.idImplemento = idImplemento;
     }
 
     public String getNombreI() {
         return nombreI;
     }
 
     public void setNombreI(String nombreI) {
         this.nombreI = nombreI;
     }
 
     public BigDecimal getPrecioUnitarioI() {
         return precioUnitarioI;
     }
 
     public void setPrecioUnitarioI(BigDecimal precioUnitarioI) {
         this.precioUnitarioI = precioUnitarioI;
     }
 
     public short getCantidad() {
         return cantidad;
     }
 
     public void setCantidad(short cantidad) {
         this.cantidad = cantidad;
     }
 
     public String getDescripcionI() {
         return descripcionI;
     }
 
     public void setDescripcionI(String descripcionI) {
         this.descripcionI = descripcionI;
     }
 
     public String getEstadoI() {
         return estadoI;
     }
 
     public void setEstadoI(String estadoI) {
         this.estadoI = estadoI;
     }
 
     public Date getFechaRegIm() {
         return fechaRegIm;
     }
 
     public void setFechaRegIm(Date fechaRegIm) {
         this.fechaRegIm = fechaRegIm;
     }
 
     public Proveedor getIdPro() {
         return idPro;
     }
 
     public void setIdPro(Proveedor idPro) {
         this.idPro = idPro;
     }
 
     public CoordTYT getCedulae() {
         return cedulae;
     }
 
     public void setCedulae(CoordTYT cedulae) {
         this.cedulae = cedulae;
     }
 
     @XmlTransient
     public List<AsigImpl> getAsigImplList() {
         return asigImplList;
     }
 
     public void setAsigImplList(List<AsigImpl> asigImplList) {
         this.asigImplList = asigImplList;
     }
 
     @XmlTransient
     public List<ActuImpl> getActuImplList() {
         return actuImplList;
     }
 
     public void setActuImplList(List<ActuImpl> actuImplList) {
         this.actuImplList = actuImplList;
     }
 
     @Override
     public int hashCode() {
         int hash = 0;
         hash += (idImplemento != null ? idImplemento.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         // TODO: Warning - this method won't work in the case the id fields are not set
         if (!(object instanceof ImplSeguridad)) {
             return false;
         }
         ImplSeguridad other = (ImplSeguridad) object;
         if ((this.idImplemento == null && other.idImplemento != null) || (this.idImplemento != null && !this.idImplemento.equals(other.idImplemento))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "com.porcupine.psp.model.entity.ImplSeguridad[ idImplemento=" + idImplemento + " ]";
     }
     
     public ImplSeguridadVO toVO() {     
         ImplSeguridadVO vo = new ImplSeguridadVO();
         vo.setIdImplemento(idImplemento);
         vo.setNombreI(nombreI);
         vo.setPrecioUnitarioI(precioUnitarioI);
         vo.setCantidad(cantidad);
         vo.setPrecioUnitarioI(precioUnitarioI);
         vo.setEstadoI(estadoI);
         vo.setFechaRegIm(fechaRegIm);
         vo.setIdPro(getIdPro().getIdPro());
         vo.setCedulaCoordTyT(getCedulae().getCedulae());
         
         ArrayList<AsigImplVO> listAsigImplVO = new ArrayList<AsigImplVO>();
         for(AsigImpl entity : getAsigImplList()) {
            listAsigImplVO.add(entity.toVO());
         }
         ArrayList<ActuImplVO> listActuImplVO = new ArrayList<ActuImplVO>();
         for(ActuImpl entity : getActuImplList()) {
            listActuImplVO.add(entity.toVO());
         }
         
         return vo;   
     }
     
 }
