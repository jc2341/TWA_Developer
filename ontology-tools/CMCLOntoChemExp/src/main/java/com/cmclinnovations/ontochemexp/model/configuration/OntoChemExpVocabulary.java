package com.cmclinnovations.ontochemexp.model.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Reads values of all OntoChemExp class and property names and name spaces
 * provided in the ontochemexp.vocabulary.properties file.</br>
 * 
 * This will empower users to use OntoChemExp, if some of its classes,
 * properties or name spaces change at a later stage, without changing its
 * source code.</br>
 * 
 * @author Feroz Farazi (msff2@cam.ac.uk)
 *
 */
@Configuration
@PropertySource("classpath:ontochemexp.vocabulary.properties")
public class OntoChemExpVocabulary {
	////////////////////////////////////////////////////////////////
	/////////////////////// OntoChemExp class////////////////////////
	////////////////////////////////////////////////////////////////
	@Value("${ontochemexp.class.experiment}")
	private String classExperiment;

	public String getClassExperiment() {
		return classExperiment;
	}

	public void setClassExperiment(String classExperiment) {
		this.classExperiment = classExperiment;
	}

	@Value("${ontochemexp.class.copyright}")
	private String classCopyright;

	public String getClassCopyright() {
		return classCopyright;
	}

	public void setClassCopyright(String classCopyright) {
		this.classCopyright = classCopyright;
	}

	@Value("${ontochemexp.class.bibliographyLink}")
	private String classBibliographyLink;

	public String getClassBibliographyLink() {
		return classBibliographyLink;
	}

	public void setClassBibliographyLink(String classBibliographyLink) {
		this.classBibliographyLink = classBibliographyLink;
	}

	@Value("${ontochemexp.class.apparatus}")
	private String classApparatus;

	public String getClassApparatus() {
		return classApparatus;
	}

	public void setClassApparatus(String classApparatus) {
		this.classApparatus = classApparatus;
	}

	@Value("${ontochemexp.class.commonProperties}")
	private String classCommonProperties;

	public String getClassCommonProperties() {
		return classCommonProperties;
	}

	public void setClassCommonProperties(String classCommonProperties) {
		this.classCommonProperties = classCommonProperties;
	}

	@Value("${ontochemexp.class.dataGroup}")
	private String classDataGroup;

	public String getClassDataGroup() {
		return classDataGroup;
	}

	public void setClassDataGroup(String classDataGroup) {
		this.classDataGroup = classDataGroup;
	}

	@Value("${ontochemexp.class.additionalDataItem}")
	private String classAdditionalDataItem;

	public String getClassAdditionalDataItem() {
		return classAdditionalDataItem;
	}

	public void setClassAdditionalDataItem(String classAdditionalDataItem) {
		this.classAdditionalDataItem = classAdditionalDataItem;
	}

	@Value("${ontochemexp.class.preferredKey}")
	private String classPreferredKey;

	public String getClassPreferredKey() {
		return classPreferredKey;
	}

	public void setClassPreferredKey(String classPreferredKey) {
		this.classPreferredKey = classPreferredKey;
	}

	@Value("${ontochemexp.class.kind}")
	private String classKind;

	public String getClassKind() {
		return classKind;
	}

	public void setClassKind(String classKind) {
		this.classKind = classKind;
	}

	@Value("${ontochemexp.class.mode}")
	private String classMode;

	public String getClassMode() {
		return classMode;
	}

	public void setClassMode(String classMode) {
		this.classMode = classMode;
	}

	@Value("${ontochemexp.class.property}")
	private String classProperty;

	public String getClassProperty() {
		return classProperty;
	}

	public void setClassProperty(String classProperty) {
		this.classProperty = classProperty;
	}

	@Value("${ontochemexp.class.initialComposition}")
	private String classInitialComposition;
	
	public String getClassInitialComposition() {
		return classInitialComposition;
	}

	public void setClassInitialComposition(String classInitialComposition) {
		this.classInitialComposition = classInitialComposition;
	}
	
	@Value("${ontochemexp.class.composition}")
	private String classComposition;
	
	public String getClassComposition() {
		return classComposition;
	}

	public void setClassComposition(String classComposition) {
		this.classComposition = classComposition;
	}
	
	@Value("${ontochemexp.class.concentration}")
	private String classConcentration;
	
	public String getClassConcentration() {
		return classConcentration;
	}

	public void setClassConcentration(String classConcentration) {
		this.classConcentration = classConcentration;
	}
	
	@Value("${ontochemexp.class.dataGroupLink}")
	private String classDataGroupLink;

	public String getClassDataGroupLink() {
		return classDataGroupLink;
	}

	public void setClassDataGroupLink(String classDataGroupLink) {
		this.classDataGroupLink = classDataGroupLink;
	}

	@Value("${ontochemexp.class.dataPoint}")
	private String classDataPoint;

	public String getClassDataPoint() {
		return classDataPoint;
	}

	public void setClassDataPoint(String classDataPoint) {
		this.classDataPoint = classDataPoint;
	}

	@Value("${ontochemexp.class.component}")
	private String classComponent;

	public String getClassComponent() {
		return classComponent;
	}

	public void setClassComponent(String classComponent) {
		this.classComponent = classComponent;
	}

	@Value("${ontochemexp.class.value}")
	private String classValue;

	public String getClassValue() {
		return classValue;
	}

	public void setClassValue(String classValue) {
		this.classValue = classValue;
	}

	@Value("${ontochemexp.class.uncertainty}")
	private String classUncertainty;

	public String getClassUncertainty() {
		return classUncertainty;
	}

	public void setClassUncertainty(String classUncertainty) {
		this.classUncertainty = classUncertainty;
	}

	@Value("${ontochemexp.class.speciesLink}")
	private String classSpeciesLink;

	public String getClassSpeciesLink() {
		return classSpeciesLink;
	}

	public void setClassSpeciesLink(String classSpeciesLink) {
		this.classSpeciesLink = classSpeciesLink;
	}

	@Value("${ontochemexp.class.derivedProperty}")
	private String classDerivedProperty;

	public String getClassDerivedProperty() {
		return classDerivedProperty;
	}

	public void setClassDerivedProperty(String classDerivedProperty) {
		this.classDerivedProperty = classDerivedProperty;
	}

	@Value("${ontochemexp.class.amount}")
	private String classAmount;

	public String getClassAmount() {
		return classAmount;
	}

	public void setClassAmount(String classAmount) {
		this.classAmount = classAmount;
	}

	@Value("${ontochemexp.class.feature}")
	private String classFeature;

	public String getClassFeature() {
		return classFeature;
	}

	public void setClassFeature(String classFeature) {
		this.classFeature = classFeature;
	}

	@Value("${ontochemexp.class.indicator}")
	private String classIndicator;

	public String getClassIndicator() {
		return classIndicator;
	}

	public void setClassIndicator(String classIndicator) {
		this.classIndicator = classIndicator;
	}

	@Value("${ontochemexp.class.observable}")
	private String classObservable;

	public String getClassObservable() {
		return classObservable;
	}

	public void setClassObservable(String classObservable) {
		this.classObservable = classObservable;
	}

	@Value("${ontochemexp.class.propertyLink}")
	private String classPropertyLink;

	public String getClassPropertyLink() {
		return classPropertyLink;
	}

	public void setClassPropertyLink(String classPropertyLink) {
		this.classPropertyLink = classPropertyLink;
	}

	@Value("${ontochemexp.class.dataAttributeLink}")
	private String classDataAttributeLink;

	public String getClassDataAttributeLink() {
		return classDataAttributeLink;
	}

	public void setClassDataAttributeLink(String classDataAttributeLink) {
		this.classDataAttributeLink = classDataAttributeLink;
	}

	@Value("${ontochemexp.class.dataPointX1}")
	private String classDataPointX1;

	public String getClassDataPointX1() {
		return classDataPointX1;
	}

	public void setClassDataPointX1(String classDataPointX1) {
		this.classDataPointX1 = classDataPointX1;
	}

	@Value("${ontochemexp.class.dataPointX2}")
	private String classDataPointX2;

	public String getClassDataPointX2() {
		return classDataPointX2;
	}

	public void setClassDataPointX2(String classDataPointX2) {
		this.classDataPointX2 = classDataPointX2;
	}

	@Value("${ontochemexp.class.dataPointX3}")
	private String classDataPointX3;

	public String getClassDataPointX3() {
		return classDataPointX3;
	}

	public void setClassDataPointX3(String classDataPointX3) {
		this.classDataPointX3 = classDataPointX3;
	}
	
	@Value("${ontochemexp.class.dataPointX4}")
	private String classDataPointX4;

	public String getClassDataPointX4() {
		return classDataPointX4;
	}

	public void setClassDataPointX4(String classDataPointX4) {
		this.classDataPointX4 = classDataPointX4;
	}
	
	@Value("${ontochemexp.class.dataPointX5}")
	private String classDataPointX5;

	public String getClassDataPointX5() {
		return classDataPointX5;
	}

	public void setClassDataPointX5(String classDataPointX5) {
		this.classDataPointX5 = classDataPointX5;
	}
	
	@Value("${ontochemexp.class.dataPointX6}")
	private String classDataPointX6;

	public String getClassDataPointX6() {
		return classDataPointX6;
	}

	public void setClassDataPointX6(String classDataPointX6) {
		this.classDataPointX6 = classDataPointX6;
	}
	
	@Value("${ontochemexp.class.dataPointX7}")
	private String classDataPointX7;

	public String getClassDataPointX7() {
		return classDataPointX7;
	}

	public void setClassDataPointX7(String classDataPointX7) {
		this.classDataPointX7 = classDataPointX7;
	}
	
	@Value("${ontochemexp.class.dataPointX8}")
	private String classDataPointX8;

	public String getClassDataPointX8() {
		return classDataPointX8;
	}

	public void setClassDataPointX8(String classDataPointX8) {
		this.classDataPointX8 = classDataPointX8;
	}
	
	@Value("${ontochemexp.class.dataPointX9}")
	private String classDataPointX9;

	public String getClassDataPointX9() {
		return classDataPointX9;
	}

	public void setClassDataPointX9(String classDataPointX9) {
		this.classDataPointX9 = classDataPointX9;
	}
	
	@Value("${ontochemexp.class.dataPointX10}")
	private String classDataPointX10;

	public String getClassDataPointX10() {
		return classDataPointX10;
	}

	public void setClassDataPointX10(String classDataPointX10) {
		this.classDataPointX10 = classDataPointX10;
	}
	
	@Value("${ontochemexp.class.dataPointX11}")
	private String classDataPointX11;

	public String getClassDataPointX11() {
		return classDataPointX11;
	}

	public void setClassDataPointX11(String classDataPointX11) {
		this.classDataPointX11 = classDataPointX11;
	}

	////////////////////////////////////////////////////////////////
	////////////////// OntoChemExp object property///////////////////
	////////////////////////////////////////////////////////////////

	@Value("${ontochemexp.object.property.hasApparatus}")
	private String objPropertyhasApparatus;

	public String getObjPropertyhasApparatus() {
		return objPropertyhasApparatus;
	}

	public void setObjPropertyhasApparatus(String objPropertyhasApparatus) {
		this.objPropertyhasApparatus = objPropertyhasApparatus;
	}

	@Value("${ontochemexp.object.property.hasAdditionalDataItem}")
	private String objPropertyhasAdditionalDataItem;

	public String getObjPropertyhasAdditionalDataItem() {
		return objPropertyhasAdditionalDataItem;
	}

	public void setObjPropertyhasAdditionalDataItem(String objPropertyhasAdditionalDataItem) {
		this.objPropertyhasAdditionalDataItem = objPropertyhasAdditionalDataItem;
	}

	@Value("${ontochemexp.object.property.hasCommonProperties}")
	private String objPropertyhasCommonProperties;

	public String getObjPropertyhasCommonProperties() {
		return objPropertyhasCommonProperties;
	}

	public void setObjPropertyhasCommonProperties(String objPropertyhasCommonProperties) {
		this.objPropertyhasCommonProperties = objPropertyhasCommonProperties;
	}

	@Value("${ontochemexp.object.property.hasCopyright}")
	private String objPropertyhasCopyright;

	public String getObjPropertyhasCopyright() {
		return objPropertyhasCopyright;
	}

	public void setObjPropertyhasCopyright(String objPropertyhasCopyright) {
		this.objPropertyhasCopyright = objPropertyhasCopyright;
	}

	@Value("${ontochemexp.object.property.hasBibliographyLink}")
	private String objPropertyhasBibliographyLink;

	public String getObjPropertyhasBibliographyLink() {
		return objPropertyhasBibliographyLink;
	}

	public void setObjPropertyhasBibliographyLink(String objPropertyhasBibliographyLink) {
		this.objPropertyhasBibliographyLink = objPropertyhasBibliographyLink;
	}

	@Value("${ontochemexp.object.property.hasDataGroup}")
	private String objPropertyhasDataGroup;

	public String getObjPropertyhasDataGroup() {
		return objPropertyhasDataGroup;
	}

	public void setObjPropertyhasDataGroup(String objPropertyhasDataGroup) {
		this.objPropertyhasDataGroup = objPropertyhasDataGroup;
	}

	@Value("${ontochemexp.object.property.hasPreferredKey}")
	private String objPropertyhasPreferredKey;

	public String getObjPropertyhasPreferredKey() {
		return objPropertyhasPreferredKey;
	}

	public void setObjPropertyhasPreferredKey(String objPropertyhasPreferredKey) {
		this.objPropertyhasPreferredKey = objPropertyhasPreferredKey;
	}

	@Value("${ontochemexp.object.property.hasKind}")
	private String objPropertyhasKind;

	public String getObjPropertyhasKind() {
		return objPropertyhasKind;
	}

	public void setObjPropertyhasKind(String objPropertyhasKind) {
		this.objPropertyhasKind = objPropertyhasKind;
	}

	@Value("${ontochemexp.object.property.hasMode}")
	private String objPropertyhasMode;

	public String getObjPropertyhasMode() {
		return objPropertyhasMode;
	}

	public void setObjPropertyhasMode(String objPropertyhasMode) {
		this.objPropertyhasMode = objPropertyhasMode;
	}

	@Value("${ontochemexp.object.property.hasProperty}")
	private String objPropertyhasProperty;

	public String getObjPropertyhasProperty() {
		return objPropertyhasProperty;
	}

	public void setObjPropertyhasProperty(String objPropertyhasProperty) {
		this.objPropertyhasProperty = objPropertyhasProperty;
	}

//	@Value("${ontochemexp.object.property.hasValue}")
//	private String objPropertyhasValue;
//
//	public String getObjPropertyhasValue() {
//		return objPropertyhasValue;
//	}
//
//	public void setObjPropertyhasValue(String objPropertyhasValue) {
//		this.objPropertyhasValue = objPropertyhasValue;
//	}

	@Value("${ontochemexp.object.property.hasUncertainty}")
	private String objPropertyhasUncertainty;

	public String getObjPropertyhasUncertainty() {
		return objPropertyhasUncertainty;
	}

	public void setObjPropertyhasUncertainty(String objPropertyhasUncertainty) {
		this.objPropertyhasUncertainty = objPropertyhasUncertainty;
	}

	@Value("${ontochemexp.object.property.hasComponent}")
	private String objPropertyhasComponent;

	public String getObjPropertyhasComponent() {
		return objPropertyhasComponent;
	}

	public void setObjPropertyhasComponent(String objPropertyhasComponent) {
		this.objPropertyhasComponent = objPropertyhasComponent;
	}

	@Value("${ontochemexp.object.property.hasDataGroupLink}")
	private String objPropertyhasDataGroupLink;

	public String getObjPropertyhasDataGroupLink() {
		return objPropertyhasDataGroupLink;
	}

	public void setObjPropertyhasDataGroupLink(String objPropertyhasDataGroupLink) {
		this.objPropertyhasDataGroupLink = objPropertyhasDataGroupLink;
	}

	@Value("${ontochemexp.object.property.hasSpeciesLink}")
	private String objPropertyhasSpeciesLink;

	public String getObjPropertyhasSpeciesLink() {
		return objPropertyhasSpeciesLink;
	}

	public void setObjPropertyhasSpeciesLink(String objPropertyhasSpeciesLink) {
		this.objPropertyhasSpeciesLink = objPropertyhasSpeciesLink;
	}

	@Value("${ontochemexp.object.property.hasDerivedProperty}")
	private String objPropertyhasDerivedProperty;

	public String getObjPropertyhasDerivedProperty() {
		return objPropertyhasDerivedProperty;
	}

	public void setObjPropertyhasDerivedProperty(String objPropertyhasDerivedProperty) {
		this.objPropertyhasDerivedProperty = objPropertyhasDerivedProperty;
	}

	@Value("${ontochemexp.object.property.hasDataPoint}")
	private String objPropertyhasDataPoint;

	public String getObjPropertyhasDataPoint() {
		return objPropertyhasDataPoint;
	}

	public void setObjPropertyhasDataPoint(String objPropertyhasDataPoint) {
		this.objPropertyhasDataPoint = objPropertyhasDataPoint;
	}

	@Value("${ontochemexp.object.property.hasAmount}")
	private String objPropertyhasAmount;

	public String getObjPropertyhasAmount() {
		return objPropertyhasAmount;
	}

	public void setObjPropertyhasAmount(String objPropertyhasAmount) {
		this.objPropertyhasAmount = objPropertyhasAmount;
	}

	@Value("${ontochemexp.object.property.hasFeature}")
	private String objPropertyhasFeature;

	public String getObjPropertyhasFeature() {
		return objPropertyhasFeature;
	}

	public void setObjPropertyhasFeature(String objPropertyhasFeature) {
		this.objPropertyhasFeature = objPropertyhasFeature;
	}

	@Value("${ontochemexp.object.property.hasIndicator}")
	private String objPropertyhasIndicator;

	public String getObjPropertyhasIndicator() {
		return objPropertyhasIndicator;
	}

	public void setObjPropertyhasIndicator(String objPropertyhasIndicator) {
		this.objPropertyhasIndicator = objPropertyhasIndicator;
	}

	@Value("${ontochemexp.object.property.hasObservable}")
	private String objPropertyhasObservable;

	public String getObjPropertyhasObservable() {
		return objPropertyhasObservable;
	}

	public void setObjPropertyhasObservable(String objPropertyhasObservable) {
		this.objPropertyhasObservable = objPropertyhasObservable;
	}

	@Value("${ontochemexp.object.property.hasPropertyLink}")
	private String objPropertyhasPropertyLink;

	public String getObjPropertyhasPropertyLink() {
		return objPropertyhasPropertyLink;
	}

	public void setObjPropertyhasPropertyLink(String objPropertyhasPropertyLink) {
		this.objPropertyhasPropertyLink = objPropertyhasPropertyLink;
	}

	@Value("${ontochemexp.object.property.hasDataAttributeLink}")
	private String objPropertyhasDataAttributeLink;

	public String getObjPropertyhasDataAttributeLink() {
		return objPropertyhasDataAttributeLink;
	}

	public void setObjPropertyhasDataAttributeLink(String objPropertyhasDataAttributeLink) {
		this.objPropertyhasDataAttributeLink = objPropertyhasDataAttributeLink;
	}

	@Value("${ontochemexp.object.property.hasDataPointX}")
	private String objPropertyhasDataPointX;

	public String getObjPropertyhasDataPointX() {
		return objPropertyhasDataPointX;
	}

	public void setObjPropertyhasDataPointX(String objPropertyhasDataPointX) {
		this.objPropertyhasDataPointX = objPropertyhasDataPointX;
	}
	
	@Value("${ontochemexp.object.property.refersTo}")
	private String objPropertyrefersTo;

	public String getObjPropertyrefersTo() {
		return objPropertyrefersTo;
	}

	public void setObjPropertyrefersTo(String objPropertyrefersTo) {
		this.objPropertyrefersTo = objPropertyrefersTo;
	}

	////////////////////////////////////////////////////////////////
	/////////////////// OntoChemExp data property////////////////////
	////////////////////////////////////////////////////////////////
	@Value("${ontochemexp.data.property.hasMIME}")
	private String dataPropertyhasMIME;

	public String getDataPropertyhasMIME() {
		return dataPropertyhasMIME;
	}

	public void setDataPropertyhasMIME(String dataPropertyhasMIME) {
		this.dataPropertyhasMIME = dataPropertyhasMIME;
	}

	@Value("${ontochemexp.data.property.hasItemType}")
	private String dataPropertyhasItemType;

	public String getDataPropertyhasItemType() {
		return dataPropertyhasItemType;
	}

	public void setDataPropertyhasItemType(String dataPropertyhasItemType) {
		this.dataPropertyhasItemType = dataPropertyhasItemType;
	}

	@Value("${ontochemexp.data.property.hasDescription}")
	private String dataPropertyhasDescription;

	public String getDataPropertyhasDescription() {
		return dataPropertyhasDescription;
	}

	public void setDataPropertyhasDescription(String dataPropertyhasDescription) {
		this.dataPropertyhasDescription = dataPropertyhasDescription;
	}

	@Value("${ontochemexp.data.property.hasPreferredKey}")
	private String dataPropertyhasPreferredKey;

	public String getDataPropertyhasPreferredKey() {
		return dataPropertyhasPreferredKey;
	}

	public void setDataPropertyhasPreferredKey(String dataPropertyhasPreferredKey) {
		this.dataPropertyhasPreferredKey = dataPropertyhasPreferredKey;
	}

	@Value("${ontochemexp.data.property.hasPrimeID}")
	private String dataPropertyhasPrimeID;

	public String getDataPropertyhasPrimeID() {
		return dataPropertyhasPrimeID;
	}

	public void setDataPropertyhasPrimeID(String dataPropertyhasPrimeID) {
		this.dataPropertyhasPrimeID = dataPropertyhasPrimeID;
	}

	@Value("${ontochemexp.data.property.hasID}")
	private String dataPropertyhasID;

	public String getDataPropertyhasID() {
		return dataPropertyhasID;
	}

	public void setDataPropertyhasID(String dataPropertyhasID) {
		this.dataPropertyhasID = dataPropertyhasID;
	}

	@Value("${ontochemexp.data.property.hasLabel}")
	private String dataPropertyhasLabel;

	public String getDataPropertyhasLabel() {
		return dataPropertyhasLabel;
	}

	public void setDataPropertyhasLabel(String dataPropertyhasLabel) {
		this.dataPropertyhasLabel = dataPropertyhasLabel;
	}

	@Value("${ontochemexp.data.property.hasDataPointForm}")
	private String dataPropertyhasDataPointForm;

	public String getDataPropertyhasDataPointForm() {
		return dataPropertyhasDataPointForm;
	}

	public void setDataPropertyhasDataPointForm(String dataPropertyhasDataPointForm) {
		this.dataPropertyhasDataPointForm = dataPropertyhasDataPointForm;
	}

	@Value("${ontochemexp.data.property.hasType}")
	private String dataPropertyhasType;

	public String getDataPropertyhasType() {
		return dataPropertyhasType;
	}

	public void setDataPropertyhasType(String dataPropertyhasType) {
		this.dataPropertyhasType = dataPropertyhasType;
	}

	@Value("${ontochemexp.data.property.hasName}")
	private String dataPropertyhasName;

	public String getDataPropertyhasName() {
		return dataPropertyhasName;
	}

	public void setDataPropertyhasName(String dataPropertyhasName) {
		this.dataPropertyhasName = dataPropertyhasName;
	}

	@Value("${ontochemexp.data.property.hasUnits}")
	private String dataPropertyhasUnits;

	public String getDataPropertyhasUnits() {
		return dataPropertyhasUnits;
	}

	public void setDataPropertyhasUnits(String dataPropertyhasUnits) {
		this.dataPropertyhasUnits = dataPropertyhasUnits;
	}

	@Value("${ontochemexp.data.property.hasDerivedPropertyExists}")
	private String dataPropertyhasDerivedPropertyExists;

	public String getDataPropertyhasDerivedPropertyExists() {
		return dataPropertyhasDerivedPropertyExists;
	}

	public void setDataPropertyhasDerivedPropertyExists(String dataPropertyhasDerivedPropertyExists) {
		this.dataPropertyhasDerivedPropertyExists = dataPropertyhasDerivedPropertyExists;
	}

	@Value("${ontochemexp.data.property.hasBound}")
	private String dataPropertyhasBound;

	public String getDataPropertyhasBound() {
		return dataPropertyhasBound;
	}

	public void setDataPropertyhasBound(String dataPropertyhasBound) {
		this.dataPropertyhasBound = dataPropertyhasBound;
	}

	@Value("${ontochemexp.data.property.hasKind}")
	private String dataPropertyhasKind;

	public String getDataPropertyhasKind() {
		return dataPropertyhasKind;
	}

	public void setDataPropertyhasKind(String dataPropertyhasKind) {
		this.dataPropertyhasKind = dataPropertyhasKind;
	}

	@Value("${ontochemexp.data.property.hasTransformation}")
	private String dataPropertyhasTransformation;

	public String getDataPropertyhasTransformation() {
		return dataPropertyhasTransformation;
	}

	public void setDataPropertyhasTransformation(String dataPropertyhasTransformation) {
		this.dataPropertyhasTransformation = dataPropertyhasTransformation;
	}

	@Value("${ontochemexp.data.property.hasVariableID}")
	private String dataPropertyhasVariableID;

	public String getDataPropertyhasVariableID() {
		return dataPropertyhasVariableID;
	}

	public void setDataPropertyhasVariableID(String dataPropertyhasVariableID) {
		this.dataPropertyhasVariableID = dataPropertyhasVariableID;
	}

	@Value("${ontochemexp.data.property.hasDataGroupID}")
	private String dataPropertyhasDataGroupID;

	public String getDataPropertyhasDataGroupID() {
		return dataPropertyhasDataGroupID;
	}

	public void setDataPropertyhasDataGroupID(String dataPropertyhasDataGroupID) {
		this.dataPropertyhasDataGroupID = dataPropertyhasDataGroupID;
	}

	@Value("${ontochemexp.data.property.hasDataPointID}")
	private String dataPropertyhasDataPointID;

	public String getDataPropertyhasDataPointID() {
		return dataPropertyhasDataPointID;
	}

	public void setDataPropertyhasDataPointID(String dataPropertyhasDataPointID) {
		this.dataPropertyhasDataPointID = dataPropertyhasDataPointID;
	}

	@Value("${ontochemexp.data.property.hasXmlns}")
	private String dataPropertyhasXmlns;

	public String getDataPropertyhasXmlns() {
		return dataPropertyhasXmlns;
	}

	public void setDataPropertyhasXmlns(String dataPropertyhasXmlns) {
		this.dataPropertyhasXmlns = dataPropertyhasXmlns;
	}

	@Value("${ontochemexp.data.property.hasXmlnsXsi}")
	private String dataPropertyhasXmlnsXsi;

	public String getDataPropertyhasXmlnsXsi() {
		return dataPropertyhasXmlnsXsi;
	}

	public void setDataPropertyhasXmlnsXsi(String dataPropertyhasXmlnsXsi) {
		this.dataPropertyhasXmlnsXsi = dataPropertyhasXmlnsXsi;
	}

	@Value("${ontochemexp.data.property.hasXsiSchemaLocation}")
	private String dataPropertyhasXsiSchemaLocation;

	public String getDataPropertyhasXsiSchemaLocation() {
		return dataPropertyhasXsiSchemaLocation;
	}

	public void setDataPropertyhasXsiSchemaLocation(String dataPropertyhasXsiSchemaLocation) {
		this.dataPropertyhasXsiSchemaLocation = dataPropertyhasXsiSchemaLocation;
	}

	@Value("${ontochemexp.data.property.hasValue}")
	private String dataPropertyhasValue;

	public String getDataPropertyhasValue() {
		return dataPropertyhasValue;
	}

	public void setDataPropertyhasValue(String dataPropertyhasValue) {
		this.dataPropertyhasValue = dataPropertyhasValue;
	}

	@Value("${ontochemexp.data.property.hasPropertyID}")
	private String dataPropertyhasPropertyID;

	public String getDataPropertyhasPropertyID() {
		return dataPropertyhasPropertyID;
	}

	public void setDataPropertyhasPropertyID(String dataPropertyhasPropertyID) {
		this.dataPropertyhasPropertyID = dataPropertyhasPropertyID;
	}
	
	@Value("${ontochemexp.data.property.hasUniqueSpeciesIRI}")
	private String dataPropertyhasUniqueSpeciesIRI;

	public String getDataPropertyhasUniqueSpeciesIRI() {
		return dataPropertyhasUniqueSpeciesIRI;
	}

	public void setDataPropertyhasUniqueSpeciesIRI(String dataPropertyhasUniqueSpeciesIRI) {
		this.dataPropertyhasUniqueSpeciesIRI = dataPropertyhasUniqueSpeciesIRI;
	}
	
	@Value("${ontochemexp.data.property.hasSourceType}")
	private String dataPropertyhasSourceType;

	public String getDataPropertyhasSourceType() {
		return dataPropertyhasSourceType;
	}

	public void setDataPropertyhasSourceType(String dataPropertyhasSourceType) {
		this.dataPropertyhasSourceType = dataPropertyhasSourceType;
	}
}
