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
import java.util.List;
import java.util.Map;

public class HandlebarsTest {
    private String readFile(String path, Charset encoding) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(path), encoding);
        return String.join(System.lineSeparator(), lines);
    }

    private void convertTemplate(File sourceFile, File destFile, String data) throws IOException {
        String source = readFile(sourceFile.getPath(), StandardCharsets.UTF_8);
        System.out.println(source);

        JsonNode jsonNode = new ObjectMapper().readValue(data, JsonNode.class);
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
        File sourceFile = new File("Helloworld.fo");
        File destFile = new File("Helloworld1.fo");

        convertTemplate(sourceFile, destFile, "{}");
    }
}
