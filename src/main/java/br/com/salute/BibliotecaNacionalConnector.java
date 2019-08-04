package br.com.salute;

import br.com.salute.dto.IsbnDTO;
import br.com.salute.interfaces.Connector;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BibliotecaNacionalConnector implements Connector {

    private static final String URL_GET_SESSION = "http://www.isbn.bn.br/website/consulta/cadastro";
    private static final String URL_GET_ISBN = "http://www.isbn.bn.br/website/consulta/cadastro/isbn/";
    private static final HttpClient httpClient = HttpClientBuilder.create().build();
    private static final BasicCookieStore cookieStore = new BasicCookieStore();
    private static final HttpContext httpContext = new BasicHttpContext();
    private static final String TITULO_PATTERN = "Título</strong><br />(.*?)<br/>";
    private static final String EDICAO_PATTERN = "Edição</strong><br />(.*?)<br/>";
    private static final String ANO_EDICAO_PATTERN = "Ano Edição</strong><br />(.*?)<br/>";
    private static final String PAGINAS_PATTERN = "Páginas</strong><br />(.*?)<br/>";
    private static final String EDITORA_PATTERN = "Editor\\(a\\)</strong><br />(.*?)<br/>";
    private static final String AUTOR_PATTERN = "<br/>(.*?)\\(Autor\\)<br/>";

    public BibliotecaNacionalConnector() {
        httpContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
    }

    public IsbnDTO recuperarIsbn(String codigoIsbn) {
        IsbnDTO dto = new IsbnDTO();
        dto.setCodigoIsbn(codigoIsbn);
        String body = recuperarPagina(codigoIsbn);
        dto.setTitulo(getTitulo(body));
        dto.setAno(Integer.valueOf(getAno(body)));
        dto.setPaginas(Integer.valueOf(getPaginas(body)));
        dto.setNomeEditora(getEditora(body));
        return dto;
    }

    private String getTitulo(String body) {
        Pattern p = Pattern.compile(TITULO_PATTERN, Pattern.MULTILINE);
        Matcher matcher = p.matcher(body);
        String resultado = null;
        if (matcher.find()) {
            resultado = matcher.group(1);
        }
        return resultado.trim();
    }

    private String getEdicao(String body) {
        Pattern p = Pattern.compile(EDICAO_PATTERN, Pattern.MULTILINE);
        Matcher matcher = p.matcher(body);
        String resultado = null;
        if (matcher.find()) {
            resultado = matcher.group(1);
        }
        return resultado.trim();
    }

    private String getAno(String body) {
        Pattern p = Pattern.compile(ANO_EDICAO_PATTERN, Pattern.MULTILINE);
        Matcher matcher = p.matcher(body);
        String resultado = null;
        if (matcher.find()) {
            resultado = matcher.group(1);
        }
        return resultado.trim();
    }

    private String getPaginas(String body) {
        Pattern p = Pattern.compile(PAGINAS_PATTERN, Pattern.MULTILINE);
        Matcher matcher = p.matcher(body);
        String resultado = null;
        if (matcher.find()) {
            resultado = matcher.group(1);
        }
        return resultado.trim();
    }

    private String getEditora(String body) {
        Pattern p = Pattern.compile(EDITORA_PATTERN, Pattern.MULTILINE);
        Matcher matcher = p.matcher(body);
        String resultado = null;
        if (matcher.find()) {
            resultado = matcher.group(1);
        }
        return resultado.trim();
    }


    private String getAutor(String body) {
        Pattern p = Pattern.compile(AUTOR_PATTERN);
        Matcher matcher = p.matcher(body);
        String resultado = null;
        if (matcher.find()) {
            resultado = matcher.group(1);
        }
        return resultado.trim();
    }

    private String recuperarPagina(String codigoIsbn) {
        StringBuffer result = new StringBuffer();
        try {
            httpClient.execute(new HttpGet(URL_GET_SESSION), httpContext); //Necessário criar sessão antes
            HttpResponse response2 = httpClient.execute(new HttpGet(URL_GET_ISBN + codigoIsbn), httpContext);
             BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response2.getEntity().getContent()));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    public static void main(String[] args) {
        BibliotecaNacionalConnector b = new BibliotecaNacionalConnector();
        IsbnDTO dto = b.recuperarIsbn("9788566250145");
        System.out.println(dto);
    }

}
