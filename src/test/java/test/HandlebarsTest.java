package test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.*;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.context.MethodValueResolver;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HandlebarsTest {
    private String readFile(String path, Charset encoding) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(path), encoding);
        return String.join(System.lineSeparator(), lines);
    }
    private String padLeft(String str, int length, String padString) {
        str += "";
        if (padString.isEmpty()) {
            padString = "0";
        }
        while (str.length() < length) {
            str = padString + str;
        }
        return str;
    }

    private void registerHelper(Handlebars handlebars) {
        handlebars.registerHelper("translate", (context, options) -> {
            String translatedStr;

            String key = options.param(0, "Key");
            String language = options.param(1, "de");

            translatedStr = key;
            return new Handlebars.SafeString(translatedStr);
        });
        handlebars.registerHelper("ifCond", (context, options) -> {
            String v1 = options.param(0, "v1");
            String operator = options.param(1, "==");
            String v2 = options.param(1, "v2");

            return switch (operator) {
                case "==", "===" -> (v1.equals(v2)) ? options.fn(this) : options.inverse(this);
                case "!==", "!=" -> (!v1.equals(v2)) ? options.fn(this) : options.inverse(this);
                case "<" -> (v1.compareTo(v2) < 0) ? options.fn(this) : options.inverse(this);
                case "<=" -> (v1.compareTo(v2) <= 0) ? options.fn(this) : options.inverse(this);
                case ">" -> (v1.compareTo(v2) > 0) ? options.fn(this) : options.inverse(this);
                case ">=" -> (v1.compareTo(v2) >= 0) ? options.fn(this) : options.inverse(this);
                case "&&" -> (!v1.isEmpty() && !v2.isEmpty()) ? options.fn(this) : options.inverse(this);
                case "||" -> (!v1.isEmpty() || !v2.isEmpty()) ? options.fn(this) : options.inverse(this);
                default -> options.inverse(this);
            };
        });
        handlebars.registerHelper("formatDate", (context, options) -> {
            String defaultFormat = "YYYYMMDDHHmmss";
            String outputFormat = options.param(0, defaultFormat);
            String inputDate = options.param(1, null);
            String inputFormat = options.param(2, null);
            boolean isDate;
            try {
                java.time.format.DateTimeFormatter.ISO_DATE_TIME.parse(inputDate);
                isDate = true;
            } catch (java.time.format.DateTimeParseException e) {
                isDate = false;
            } catch (NullPointerException e) {
                isDate = false;
            }
            String date;
            if (inputDate != null) {
                if (!isDate && inputFormat == null) {
                    inputFormat = (inputDate.length() == 8) ? "YYYYMMDD" : defaultFormat;
                }
                date = java.time.format.DateTimeFormatter.ofPattern(inputFormat).parse(inputDate).toString();
            } else {
                date = LocalDate.now().toString();
            }

            return new Handlebars.SafeString(date.format(outputFormat));
        });
        handlebars.registerHelper("kwl:value", (context, options) -> {
            String delimiter = "-";
            Pattern regExp = Pattern.compile(delimiter + "\\s(.*)", Pattern.MULTILINE);
            String keyValue = options.param(0, "");

            String result = "";
            Matcher matcher = regExp.matcher(keyValue);
            if (matcher.find()) {
                result = matcher.group();
            }

            return new Handlebars.SafeString(result);            });
        handlebars.registerHelper("padLeft", (context, options) -> {
            String str = options.param(0, "");
            String padLeft = options.param(1, "");

            return new Handlebars.SafeString(padLeft(str, padLeft.length(), padLeft.substring(0,1)));
        });
        handlebars.registerHelper("htmlToFo", (context, options) -> {
            String str = options.param(0, "");
            // TODO implement
            return new Handlebars.SafeString(str);
        });
        handlebars.registerHelper("base64Image", (context, options) -> {
            String str = options.param(0, "");
            // TODO implement
            return new Handlebars.SafeString(str);
        });
    }

    private void convertTemplate(File sourceFile, File destFile, String data) throws IOException {
        String source = readFile(sourceFile.getPath(), StandardCharsets.UTF_8);
        System.out.println(source);

        JsonNode jsonNode = new ObjectMapper().readValue(data, JsonNode.class);
        Handlebars handlebars = new Handlebars();
        registerHelper(handlebars);
        handlebars.registerHelper("json", Jackson2Helper.INSTANCE);
        Context context = Context
                .newBuilder(jsonNode)
                .resolver(JsonNodeValueResolver.INSTANCE,
                        JavaBeanValueResolver.INSTANCE,
                        FieldValueResolver.INSTANCE,
                        MapValueResolver.INSTANCE,
                        MethodValueResolver.INSTANCE
                )
                .build();

        Template template = handlebars.compileInline(source);
        System.out.println(template.apply(context));
        String dest = template.apply(context);

        FileWriter fw = new FileWriter(destFile);
        fw.write(dest);
        fw.close();

    }
    @Test
    public void TestHandlebarsJackson() throws IOException {
        String json = "{\"name\": \"world\"}";
        JsonNode jsonNode = new ObjectMapper().readValue(json, JsonNode.class);
        Handlebars handlebars = new Handlebars();
        handlebars.registerHelper("json", Jackson2Helper.INSTANCE);

        Context context = Context
                .newBuilder(jsonNode)
                .resolver(JsonNodeValueResolver.INSTANCE,
                        JavaBeanValueResolver.INSTANCE,
                        FieldValueResolver.INSTANCE,
                        MapValueResolver.INSTANCE,
                        MethodValueResolver.INSTANCE
                )
                .build();
        Template template = handlebars.compileInline("Hello {{name}}!");
        System.out.println(template.apply(context));
    }
    @Test
    public void TestHandlebarsGson() throws IOException {
        String data = "{\"subject\": \"World\"}";
        String decoration = "Hello {{subject}}!";

        Handlebars handlebars = new Handlebars();
        Gson gson = new Gson();

        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> map = gson.fromJson(data, type);

        Template template = handlebars.compileInline(decoration);
        Context context = Context.newBuilder(map).build();

        System.out.println(template.apply(context));    }
    @Test
    public void TestConvertTemplate() throws IOException {
        File sourceFile = new File("Extended.fo");
        File destFile = new File("Extended1.fo");

        convertTemplate(sourceFile, destFile, "{}");
    }
}
