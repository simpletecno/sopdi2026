/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.simpletecno.sopdi.seguridad;

import com.simpletecno.sopdi.contabilidad.EmpresaCuentasEquivalentesHelper;
import com.simpletecno.sopdi.extras.infile.Emisor;
import com.vaadin.server.StreamResource;

/**
 *
 * @author joseaguirre
 */
public class SessionInformation {

    private String strSessionId;
    private String strUserId;
    private String strUserName;
    private String strUserFullName;
    private String strUserProfile;
    private String strUserProfileName;
    private String strCompanyId = "";
    private String strCompanyName = "";
    private String strAliasName;
    private String strProjectId = "";
    private String strProjectName = "";
    private StreamResource projectStreamResource;
    private String strProjectReserveAmount;
    private String strProjectGestionAmount;
    private String strProjectCurrency;
    private StreamResource photoStreamResource;
    private String strLastLogin;
    private Double dblBudgetCharge;
    private String strUserSpecialCode;
    private String strAccountingCompanyId = "";
    private String strAccountingCompanyName = "";
    private String strAccountingCompanyTaxId = "";
    private String strAccountingCompanyRegimen = "";
    private String strAccountingCompanySmallName = "";
    private String strUserToken = "";
    private float fltExchangeRate;
    private String strAccountingCompanyFelUser = "";
    private String strAccountingCompanyFelPass = "";
    private String strAccountingCompanyFelToken = "";
    private String strAccountingCompanyFelCodProdExento = "";
    private String strGupoTrabajo = "";
    private String strIdProveedor = "";
    private int diasAntesHoyFechaTareaProgramada;
    private int diasDespuesHoyFechaTareaProgramada;
    private String strAccountingCompanyDirection = "";
    private Emisor emisor = null;
    public EmpresaCuentasEquivalentesHelper empresaCuentasEquivalentesHelper;

    /**
     * @return the strSessionId
     */
    public String getStrSessionId() {
        return strSessionId;
    }

    /**
     * @param strSessionId the strSessionId to set
     */
    public void setStrSessionId(String strSessionId) {
        this.strSessionId = strSessionId;
    }

    /**
     * @return the strUserName
     */
    public String getStrUserId() {
        return strUserId;
    }

    /**
     * @param strUserId the strUserName to set
     */
    public void setStrUserId(String strUserId) {
        this.strUserId = strUserId;
    }

    /**
     * @return the strUserProfile
     */
    public String getStrUserProfile() {
        return strUserProfile;
    }

    /**
     * @param strUserProfile the strUserProfile to set
     */
    public void setStrUserProfile(String strUserProfile) {
        this.strUserProfile = strUserProfile;
    }

    /**
     * @return the strUserProfileName
     */
    public String getStrUserProfileName() {
        return strUserProfileName;
    }

    /**
     * @param strUserProfileName the strUserProfileName to set
     */
    public void setStrUserProfileName(String strUserProfileName) {
        this.strUserProfileName = strUserProfileName;
    }

    /**
     * @return the strCompanyId
     */
    public String getStrCompanyId() {
        return strCompanyId;
    }

    /**
     * @param strCompanyId the strCompanyId to set
     */
    public void setStrCompanyId(String strCompanyId) {
        this.strCompanyId = strCompanyId;
    }

    /**
     * @return the strUserFullName
     */
    public String getStrUserName() {
        return strUserName;
    }

    /**
     * @param strUserName the strUserName to set
     */
    public void setStrUserName(String strUserName) {
        this.strUserName = strUserName;
    }

    /**
     * @return the strCompanyName
     */
    public String getStrCompanyName() {
        return strCompanyName;
    }

    /**
     * @param strCompanyName the strCompanyName to set
     */
    public void setStrCompanyName(String strCompanyName) {
        this.strCompanyName = strCompanyName;
    }

    public String getStrAccountingCompanyTaxId() {
        return strAccountingCompanyTaxId;
    }

    public void setStrAccountingCompanyTaxId(String strAccountingCompanyTaxId) {
        this.strAccountingCompanyTaxId = strAccountingCompanyTaxId;
    }

    /**
     * @return the strAliasName
     */
    public String getStrAliasName() {
        return strAliasName;
    }

    /**
     * @param strAliasName the strAliasName to set
     */
    public void setStrAliasName(String strAliasName) {
        this.strAliasName = strAliasName;
    }

    /**
     * @return the strUserFullName
     */
    public String getStrUserFullName() {
        return strUserFullName;
    }

    /**
     * @param strUserFullName the strUserFullName to set
     */
    public void setStrUserFullName(String strUserFullName) {
        this.strUserFullName = strUserFullName;
    }

    /**
     * @return the photoStreamResource
     */
    public StreamResource getPhotoStreamResource() {
        return photoStreamResource;
    }

    /**
     * @param photoStreamResource the photoStreamResource to set
     */
    public void setPhotoStreamResource(StreamResource photoStreamResource) {
        this.photoStreamResource = photoStreamResource;
    }

    /**
     * @return the strProjectId
     */
    public String getStrProjectId() {
        return strProjectId;
    }

    /**
     * @param strProjectId the strProjectId to set
     */
    public void setStrProjectId(String strProjectId) {
        this.strProjectId = strProjectId;
    }

    /**
     * @return the strProjectName
     */
    public String getStrProjectName() {
        return strProjectName;
    }

    /**
     * @param strProjectName the strProjectName to set
     */
    public void setStrProjectName(String strProjectName) {
        this.strProjectName = strProjectName;
    }

    /**
     * @return the projectStreamResource
     */
    public StreamResource getProjectStreamResource() {
        return projectStreamResource;
    }

    /**
     * @param projectStreamResource the projectStreamResource to set
     */
    public void setProjectStreamResource(StreamResource projectStreamResource) {
        this.projectStreamResource = projectStreamResource;
    }

    /**
     * @return the strProjectGestionAmount
     */
    public String getStrProjectGestionAmount() {
        return strProjectGestionAmount;
    }

    /**
     * @param strProjectGestionAmount the strProjectGestionAmount to set
     */
    public void setStrProjectGestionAmount(String strProjectGestionAmount) {
        this.strProjectGestionAmount = strProjectGestionAmount;
    }

    /**
     * @return the strProjectReserveAmount
     */
    public String getStrProjectReserveAmount() {
        return strProjectReserveAmount;
    }

    /**
     * @param strProjectReserveAmount the strProjectReserveAmount to set
     */
    public void setStrProjectReserveAmount(String strProjectReserveAmount) {
        this.strProjectReserveAmount = strProjectReserveAmount;
    }

    /**
     * @return the strLastLogin
     */
    public String getStrLastLogin() {
        return strLastLogin;
    }

    /**
     * @param strLastLogin the strLastLogin to set
     */
    public void setStrLastLogin(String strLastLogin) {
        this.strLastLogin = strLastLogin;
    }

    /**
     * @return the strProjectCurrency
     */
    public String getStrProjectCurrency() {
        return strProjectCurrency;
    }

    /**
     * @param strProjectCurrency the strProjectCurrency to set
     */
    public void setStrProjectCurrency(String strProjectCurrency) {
        this.strProjectCurrency = strProjectCurrency;
    }

    /**
     * @return the dblBudgetCharge
     */
    public Double getDblBudgetCharge() {
        return dblBudgetCharge;
    }

    /**
     * @param dblBudgetCharge the dblBudgetCharge to set
     */
    public void setDblBudgetCharge(Double dblBudgetCharge) {
        this.dblBudgetCharge = dblBudgetCharge;
    }

    /**
     * @return the strUserSpecialCode
     */
    public String getStrUserSpecialCode() {
        return strUserSpecialCode;
    }

    /**
     * @param strUserSpecialCode the strUserSpecialCode to set
     */
    public void setStrUserSpecialCode(String strUserSpecialCode) {
        this.strUserSpecialCode = strUserSpecialCode;
    }
    
    /**
     * @return the strAccountingCompanyId
     */
    public String getStrAccountingCompanyId() {
        return strAccountingCompanyId;
    }

    /**
     * @param strAccountingCompanyId the strAccountingCompanyId to set
     */
    public void setStrAccountingCompanyId(String strAccountingCompanyId) {
        this.strAccountingCompanyId = strAccountingCompanyId;
    }

    /**
     * @return the strAccountingCompanyName
     */
    public String getStrAccountingCompanyName() {
        return strAccountingCompanyName;
    }

    /**
     * @param strAccountingCompanyName the strAccountingCompanyName to set
     */
    public void setStrAccountingCompanyName(String strAccountingCompanyName) {
        this.strAccountingCompanyName = strAccountingCompanyName;
    }

    /**
     * @return the strAccountingCompanyDirection
     */
    public String getStrAccountingCompanyDirection() {
        return strAccountingCompanyDirection;
    }

    /**
     * @param strAccountingCompanyDirection the strAccountingCompanySmallName to set
     */
    public void setStrAccountingCompanyDirection(String strAccountingCompanyDirection) {
        this.strAccountingCompanyDirection = strAccountingCompanyDirection;
    }

    /**
     * @return the strAccountingCompanyDirection
     */
    public String getStrAccountingCompanyBillingDirection() {
        return strAccountingCompanyDirection;
    }

    /**
     * @param strAccountingCompanyDirection the strAccountingCompanySmallName to set
     */
    public void setStrAccountingCompanyBillingDirection(String strAccountingCompanyDirection) {
        this.strAccountingCompanyDirection = strAccountingCompanyDirection;
    }

    /**
     * @return the strAccountingCompanySmallName
     */
    public String getStrAccountingCompanySmallName() {
        return strAccountingCompanySmallName;
    }

    /**
     * @param strAccountingCompanySmallName the strAccountingCompanySmallName to set
     */
    public void setStrAccountingCompanySmallName(String strAccountingCompanySmallName) {
        this.strAccountingCompanySmallName = strAccountingCompanySmallName;
    }

    /**
     * @return the fltExchangeRate
     */
    public float getFltExchangeRate() {
        return fltExchangeRate;
    }

    /**
     * @param fltExchangeRate the dblExchangeRate to set
     */
    public void setFltlExchangeRate(float fltExchangeRate) {
        this.fltExchangeRate = fltExchangeRate;
    }

    public String getStrAccountingCompanyRegimen() {
        return strAccountingCompanyRegimen;
    }

    public void setStrAccountingCompanyRegimen(String strAccountingCompanyRegimen) {
        this.strAccountingCompanyRegimen = strAccountingCompanyRegimen;
    }

    public String getStrUserToken() {
        return strUserToken;
    }

    public void setStrUserToken(String strUserToken) {
        this.strUserToken = strUserToken;
    }

    public void setFltExchangeRate(float fltExchangeRate) {
        this.fltExchangeRate = fltExchangeRate;
    }

    public String getStrAccountingCompanyFelUser() {
        return strAccountingCompanyFelUser;
    }

    public void setStrAccountingCompanyFelUser(String strAccountingCompanyFelUser) {
        this.strAccountingCompanyFelUser = strAccountingCompanyFelUser;
    }

    public String getStrAccountingCompanyFelPass() {
        return strAccountingCompanyFelPass;
    }

    public void setStrAccountingCompanyFelPass(String strAccountingCompanyFelPass) {
        this.strAccountingCompanyFelPass = strAccountingCompanyFelPass;
    }

    public String getStrAccountingCompanyFelToken() {
        return strAccountingCompanyFelToken;
    }

    public void setStrAccountingCompanyFelToken(String strAccountingCompanyFelToken) {
        this.strAccountingCompanyFelToken = strAccountingCompanyFelToken;
    }

    public String getStrGupoTrabajo() {
        return strGupoTrabajo;
    }

    public void setStrGupoTrabajo(String strGupoTrabajo) {
        this.strGupoTrabajo = strGupoTrabajo;
    }

    public String getStrIdProveedor() {
        return strIdProveedor;
    }

    public void setStrIdProveedor(String strIdProveedor) {
        this.strIdProveedor = strIdProveedor;
    }

    public int getDiasDespuesHoyFechaTareaProgramada() {
        return diasDespuesHoyFechaTareaProgramada;
    }

    public void setDiasDespuesHoyFechaTareaProgramada(int diasDespuesHoyFechaTareaProgramada) {
        this.diasDespuesHoyFechaTareaProgramada = diasDespuesHoyFechaTareaProgramada;
    }

    public int getDiasAntesHoyFechaTareaProgramada() {
        return diasAntesHoyFechaTareaProgramada;
    }

    public void setDiasAntesHoyFechaTareaProgramada(int diasAntesHoyFechaTareaProgramada) {
        this.diasAntesHoyFechaTareaProgramada = diasAntesHoyFechaTareaProgramada;
    }

    public String getStrAccountingCompanyFelCodProdExento() {
        return strAccountingCompanyFelCodProdExento;
    }

    public void setStrAccountingCompanyFelCodProdExento(String strAccountingCompanyFelCodProdExento) {
        this.strAccountingCompanyFelCodProdExento = strAccountingCompanyFelCodProdExento;
    }

    public void setInfileEmisor (Emisor emisor) {this.emisor = emisor;}
    public Emisor getInfileEmisor() {return this.emisor;}

    public void setEmpresaCuentasEquivalentesHelper (EmpresaCuentasEquivalentesHelper helper) {this.empresaCuentasEquivalentesHelper = helper;}
    public EmpresaCuentasEquivalentesHelper getEmpresaCuentasEquivalentesHelper() {return this.empresaCuentasEquivalentesHelper;}
}
