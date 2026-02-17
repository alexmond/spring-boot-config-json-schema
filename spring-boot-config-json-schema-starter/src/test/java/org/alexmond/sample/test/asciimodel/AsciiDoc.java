package org.alexmond.sample.test.asciimodel;

import lombok.Builder;
import lombok.Data;
import org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaProperties;
import org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaType;

@Data
@Builder(builderClassName = "Builder", toBuilder = true)
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PACKAGE)
public class AsciiDoc {

    private String description = "";
    private String example = "";
    private String type = "";
    private String pattern = "";
    private String defaultValue = "";
    private Boolean deprecated = false;

    public AsciiDoc(JsonSchemaProperties jsonSchemaProperties) {
        this.description = jsonSchemaProperties == null || jsonSchemaProperties.getDescription() == null ? "" : escapeForAsciiDoc(jsonSchemaProperties.getDescription());
        this.type = jsonSchemaProperties.getType().getValue();
//        this.pattern = jsonSchemaProperties.getHint() != null ? jsonSchemaProperties.getHint().getPattern() : null;
//        this.example = jsonSchemaProperties.getHint() != null ? jsonSchemaProperties.getHint().getExample() : null;
        this.example = jsonSchemaProperties.getExamples() != null ?
                escapeForAsciiDoc(String.join("\n", jsonSchemaProperties.getExamples())) : "";
        this.defaultValue = jsonSchemaProperties.getDefaultValue() != null ?
                escapeForAsciiDoc(jsonSchemaProperties.getDefaultValue().toString()) : "";
        this.deprecated = jsonSchemaProperties.getDeprecated();
        if (jsonSchemaProperties.getEnumValues() != null) {
            type = "enum";
            this.example = escapeForAsciiDoc(String.join("\n", jsonSchemaProperties.getEnumValues()));
        }
        if (jsonSchemaProperties.getType() == JsonSchemaType.ARRAY)
            if(jsonSchemaProperties.getItems() != null) {
                if(jsonSchemaProperties.getItems().getType() == JsonSchemaType.STRING){
                    type = "string array";
            }
        }
    }

    public static String escapeForAsciiDoc(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("|", "\\|");
//                .replace("*", "\\*")
//                .replace("_", "\\_")
//                .replace("+", "\\+")
//                .replace("#", "\\#")
//                .replace("`", "\\`")
//                .replace("^", "\\^")
//                .replace("~", "\\~")
//                .replace("[", "\\[")
//                .replace("]", "\\]")
//                .replace("{", "\\{")
//                .replace("}", "\\}");
    }


}
