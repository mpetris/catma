package de.catma.repository.jsonld;

import com.jsoniter.JsonIterator;
import com.jsoniter.output.JsonStream;
import de.catma.document.Range;
import de.catma.document.standoffmarkup.usermarkup.TagReference;
import de.catma.tag.*;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import static org.junit.Assert.*;

@RunWith(JMockit.class)
public class TagReferenceJsonLdTest {
	private String original = "{\n" +
			"\t\"@context\": \"http://www.w3.org/ns/anno.jsonld\",\n" +
			"\t\"type\": \"Annotation\",\n" +
			"\t\"id\": \"http://catma.de/portal/annotation/CATMA_4711\",\n" +
			"\t\"body\": {\n" +
			"\t\t\"@context\": {\n" +
			"\t\t\t\"myProp1\": \"http://catma.de/portal/tag/CATMA_789456/property/CATMA_554\"\n" +
			"\t\t},\n" +
			"\t\t\"type\": \"Dataset\",\n" +
			"\t\t\"http://catma.de/portal/tag\": \"http://catma.de/portal/tag/CATMA_789456\",\n" +
			"\t\t\"myProp1\": \"myVal\"\n" +
			"\t},\n" +
			"\t\"target\": {\n" +
			"\t\t\"source\": \"http://catma.de/sourcedocument/doc1\",\n" +
			"\t\t\"TextPositionSelector\": {\n" +
			"\t\t\t\"start\": 42,\n" +
			"\t\t\t\"end\": 125\n" +
			"\t\t}\n" +
			"\t}\n" +
			"}";

	private String lessSimple = "{\n" +
			"\t\"@context\": \"http://www.w3.org/ns/anno.jsonld\",\n" +
			"\t\"type\": \"Annotation\",\n" +
			"\t\"id\": \"http://catma.de/portal/annotation/CATMA_4711\",\n" +
			"\t\"body\": {\n" +
			"\t\t\"@context\": {\n" +
			"\t\t\t\"myProp1\": \"http://catma.de/portal/tag/CATMA_789456/property/CATMA_554\",\n" +
			"\t\t\t\"tag\": \"http://catma.de/portal/tag\"\n" +
			"\t\t},\n" +
			"\t\t\"type\": \"Dataset\",\n" +
			"\t\t\"tag\": \"http://catma.de/portal/tag/CATMA_789456\",\n" +
			"\t\t\"properties\": {\"myProp1\": \"myVal\"}\t\t\n" +
			"\t},\n" +
			"\t\"target\": {\n" +
			"\t\t\"source\": \"http://catma.de/sourcedocument/doc1\",\n" +
			"\t\t\"TextPositionSelector\": {\n" +
			"\t\t\t\"start\": 42,\n" +
			"\t\t\t\"end\": 125\n" +
			"\t\t}\n" +
			"\t}\n" +
			"}";

	@Test
	public void serializeToJsonLd() throws Exception {
		String uri = "http://catma.de/sourcedocument/doc1";

		Range range = new Range(42, 125);

		PropertyPossibleValueList possibleValueList = new PropertyPossibleValueList("TestPossibleValue");
		PropertyDefinition propertyDefinition = new PropertyDefinition(
			1, "CATMA_PROPDEF", "FAKE_PROP_DEF", possibleValueList
		);

		TagDefinition tagDefinition = new TagDefinition(
			1, "CATMA_1", "Weather", new Version(), null, null
		);
		tagDefinition.addUserDefinedPropertyDefinition(propertyDefinition);

		PropertyValueList instancePropertyValueList = new PropertyValueList("SimplePropertyValue");
		Property property = new Property(propertyDefinition, instancePropertyValueList);

		TagInstance tagInstance = new TagInstance("CATMA_129837", tagDefinition);
		tagInstance.addUserDefinedProperty(property);

		TagReference internalReference = new TagReference(tagInstance, uri, range);
		TagReferenceJsonLd ldWrapper = new TagReferenceJsonLd(internalReference);

		String serialized = ldWrapper.serialize();

		Logger.getLogger("TagReferenceJsonLdTest").info(serialized);

		assertNotNull(serialized);
	}

	@Test
	public void deserializeFromJsonLd() throws Exception {
		TagReferenceJsonLd tagReferenceJsonLdMock = new TagReferenceJsonLd();

		InputStream inputStream = new ByteArrayInputStream(this.lessSimple.getBytes(StandardCharsets.UTF_8.name()));

		Version version = new Version();
		TagDefinition fakeTagDefinition = new TagDefinition(
			1, "CATMA_TAGDEFINITION", "FAKE_TAG_DEFINITION", version, null, null
		);

		new Expectations(tagReferenceJsonLdMock) {{
			tagReferenceJsonLdMock.findTagDefinitionForTagInstance(withInstanceOf(String.class));
			result = fakeTagDefinition;
		}};

		TagReferenceJsonLd deserialized = tagReferenceJsonLdMock.deserialize(inputStream);

		new Verifications() {{
			tagReferenceJsonLdMock.findTagDefinitionForTagInstance(withInstanceOf(String.class));
		}};

		assertNotNull(deserialized);
		assertEquals(
			fakeTagDefinition.getUuid(), deserialized.getTagReference().getTagInstance().getTagDefinition().getUuid()
		);
		assertEquals(
			"CATMA_554",
			deserialized.getTagReference().getTagInstance().getUserDefinedProperty("CATMA_554").getName()
		);
	}

	@Test
	public void jsonIterDeserializeFromJsonLdIntoIntermediate() throws Exception {
		InputStream inputStream = new ByteArrayInputStream(this.lessSimple.getBytes(StandardCharsets.UTF_8.name()));

		JsonIterator iter = JsonIterator.parse(inputStream, 128);
		TagInstanceLd deserialized = iter.read(TagInstanceLd.class);
		iter.close();

		assertNotNull(deserialized);

		assertEquals("http://www.w3.org/ns/anno.jsonld", deserialized.context);
		assertEquals("Annotation", deserialized.type);
		assertEquals("http://catma.de/portal/annotation/CATMA_4711", deserialized.id);

		assertNotNull(deserialized.body);
		assertEquals(
			"http://catma.de/portal/tag/CATMA_789456/property/CATMA_554",
			deserialized.body.context.get("myProp1")
		);
		assertEquals("http://catma.de/portal/tag", deserialized.body.context.get("tag"));
		assertEquals("Dataset", deserialized.body.type);
		assertEquals("http://catma.de/portal/tag/CATMA_789456", deserialized.body.tag);
		assertEquals("myVal", deserialized.body.properties.get("myProp1"));

		assertNotNull(deserialized.target);
		assertEquals("http://catma.de/sourcedocument/doc1", deserialized.target.source);
		assertEquals(42, deserialized.target.TextPositionSelector.start);
		assertEquals(125, deserialized.target.TextPositionSelector.end);

		String result = JsonStream.serialize(deserialized);

		Logger.getLogger("TagReferenceJsonLdTest").info(result);
	}
}
