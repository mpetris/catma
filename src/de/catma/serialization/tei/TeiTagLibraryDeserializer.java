package de.catma.serialization.tei;

import nu.xom.Elements;
import nu.xom.Nodes;
import de.catma.core.ExceptionHandler;
import de.catma.core.tag.PropertyDefinition;
import de.catma.core.tag.PropertyPossibleValueList;
import de.catma.core.tag.TagDefinition;
import de.catma.core.tag.TagLibrary;
import de.catma.core.tag.TagsetDefinition;
import de.catma.core.tag.Version;

public class TeiTagLibraryDeserializer {
	
	private TeiDocument teiDocument;
	private TagLibrary tagLibrary;

	public TeiTagLibraryDeserializer(TeiDocument teiDocument) {
		super();
		this.teiDocument = teiDocument;
		this.tagLibrary = new TagLibrary(teiDocument.getName());
		deserialize();
	}

	private void deserialize() {
		Nodes tagsetDefinitionElements = teiDocument.getTagsetDefinitionElements();
		
		for (int i=0; i<tagsetDefinitionElements.size(); i++) {
			TeiElement tagsetDefinitionElement = (TeiElement)tagsetDefinitionElements.get(i);
			String nValue = tagsetDefinitionElement.getAttributeValue(Attribute.n);
			int dividerPos = nValue.lastIndexOf(' ');
			String tagsetName = nValue.substring(0, dividerPos);
			String versionString = nValue.substring(dividerPos+1);
			TagsetDefinition tagsetDefinition = 
					new TagsetDefinition(
							tagsetDefinitionElement.getID(),tagsetName, new Version(versionString));
			
			addTagDefinitions(tagsetDefinition, tagsetDefinitionElement.getChildElements(TeiElementName.fsDecl));

			this.tagLibrary.add(tagsetDefinition);
		}
	}

	private void addTagDefinitions(
			TagsetDefinition tagsetDefinition, Elements tagDefinitionElements) {
		
		for (int i=0; i<tagDefinitionElements.size(); i++) {
			TeiElement tagDefinitionElement = (TeiElement)tagDefinitionElements.get(i);
			TeiElement descriptionElement = 
					tagDefinitionElement.getFirstTeiChildElement(TeiElementName.fsDescr);
			
			String description = "";
			
			if ((descriptionElement != null) && (descriptionElement != null)) {
				description = descriptionElement.getValue();
			}
			
			TagDefinition tagDef = 
					new TagDefinition(
							tagDefinitionElement.getID(), 
							description,
							new Version(tagDefinitionElement.getAttributeValue(Attribute.n)), 
							tagDefinitionElement.getAttributeValue(Attribute.fsDecl_baseTypes));
			
			tagsetDefinition.addTagDefinition(tagDef);
			
			addProperties(tagDef, 
					tagDefinitionElement.getChildNodes(
							TeiElementName.fDecl, 
							AttributeValue.f_Decl_name_catma_system_property.getStartsWith()),
					tagDefinitionElement.getChildNodes(
							TeiElementName.fDecl, 
							AttributeValue.f_Decl_name_catma_system_property.getNotStartsWith()));
		}
		
	}

	private void addProperties(
			TagDefinition tagDef, Nodes systemPropertyNodes, Nodes userPropertyNodes) {


		for (int i=0; i<systemPropertyNodes.size(); i++) {
			try {
				TeiElement sysPropElement = (TeiElement)systemPropertyNodes.get(i);
				PropertyDefinition pd = createPropertyDefinition(sysPropElement);
				tagDef.addSystemPropertyDefinition(pd);
			}
			catch(UnknownElementException uee) {
				ExceptionHandler.log(uee);
			}
				
		}
		
		for (int i=0; i<userPropertyNodes.size(); i++) {
			try {
				TeiElement sysPropElement = (TeiElement)userPropertyNodes.get(i);
				PropertyDefinition pd = createPropertyDefinition(sysPropElement);
				tagDef.addUserDefinedPropertyDefinition(pd);
			}
			catch(UnknownElementException uee) {
				ExceptionHandler.log(uee);
			}
				
		}
	}

	private PropertyDefinition createPropertyDefinition(
			TeiElement sysPropElement) throws UnknownElementException {
		
		TeiElement valueElement = (TeiElement)sysPropElement.getChildElements().get(0);
		
		PropertyValueFactory pvf = null;
		
		if (valueElement.is(TeiElementName.numeric)) {
			pvf = new NumericPropertyValueFactory(sysPropElement);
		}
		else if (valueElement.is(TeiElementName.string)) {
			pvf = new StringPropertyValueFactory(sysPropElement);
		}
		else if (valueElement.is(TeiElementName.vRange)) {
			pvf = new ValueRangePropertyValueFactory(sysPropElement);
		}
		else {
			throw new UnknownElementException(valueElement.getLocalName() + " is not supported!");
		}
		
		return new PropertyDefinition(
						sysPropElement.getAttributeValue(Attribute.fDecl_name),
						new PropertyPossibleValueList(pvf.getValueAsList(), 
								pvf.isSingleSelectValue()));
	}

	public TagLibrary getTagLibrary() {
		return tagLibrary;
	}
}