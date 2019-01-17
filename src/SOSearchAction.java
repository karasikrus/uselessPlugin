import com.intellij.ide.BrowserUtil;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.PsiFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.awt.RelativePoint;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

public class SOSearchAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        String languageTag = "";
        try {
            Language lang = file.getLanguage();
            languageTag = "+["+ lang.getDisplayName().toLowerCase()+"]";
        } catch (NullPointerException e1){
            e1.printStackTrace();
        }


        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        CaretModel caretModel = editor.getCaretModel();
        String selectedText = caretModel.getCurrentCaret().getSelectedText();
        ///-------------------------------------------------
        //System.out.println("Hello World!");
        String sURLBeginning = "https://api.stackexchange.com/2.2/search/advanced?order=desc&sort=votes&q=";
        String sURLEnd = "&site=stackoverflow&filter=!)re8-BDQ_iYleYt62boX";
        String sURL = sURLBeginning+selectedText.replace(' ','+')+"&tagged="+languageTag+sURLEnd; //json of questions
        //String sURL = "https://sme.tinkoff.ru/api/v1/company";
        // Connect to the URL using java's native library
        String sURL2Beginning = "https://api.stackexchange.com/2.2/questions/";
        String sURL2End = "/answers?order=desc&sort=votes&site=stackoverflow&filter=!-.3J6ZlvCNE5";
        URL url = null;
        try {
            url = new URL(sURL);
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        }
        //URLConnection request = url.openConnection();
        //request.connect();

        String source = null;
        try {
            source = IOUtils.toString(new GZIPInputStream(url.openStream()),"utf-8");
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        JSONObject json = new JSONObject(source);
        JSONArray questions = json.getJSONArray("items");
        Iterator qIt = questions.iterator();
        JSONObject question = (JSONObject) qIt.next();
        String questionId = String.valueOf((Integer) question.get("question_id"));
        String questionLink = (String) question.get("link");
        //JSONObject json = JsonReader.readJsonFromUrl(sURL);
        /*// Convert to a JSON object to print data
        JsonParser jp = new JsonParser(); //from gson
        JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element
        JsonObject rootobj = root.getAsJsonObject(); //May be an array, may be an object.
        //String zipcode = rootobj.get("zip_code").getAsString(); //just grab the zipcode
        String myjson = rootobj.toString();*/
       //System.out.println(questionId);
        String sURL2 = sURL2Beginning+questionId+sURL2End;
        try {
            url=new URL(sURL2);
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        }
        try {
            source = IOUtils.toString(new GZIPInputStream(url.openStream()),"utf-8");
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        json = new JSONObject(source);
        JSONArray answers = json.getJSONArray("items");
        qIt = answers.iterator();
        JSONObject answer = (JSONObject) qIt.next();
        String answerBody = (String) answer.get("body");
       String shown = "<a href=\""+questionLink+"\" rel=\"noreferrer\"><b>Open in browser</b></a> "+"\n"+answerBody;
           // String shown = "<p>Consider using <a href=\"http://download.oracle.com/javase/6/docs/api/java/net/URLConnection.html\" rel=\"noreferrer\"><code>URLConnection</code></a> instead. Furthermore you might want to leverage <a href=\"http://commons.apache.org/io/apidocs/org/apache/commons/io/IOUtils.html\" rel=\"noreferrer\"><code>IOUtils</code></a> from <a href=\"http://commons.apache.org/io/\" rel=\"noreferrer\">Apache Commons IO</a> to make the string reading easier too. For example:</p>";
        //System.out.println("<p>Consider using <a href=\"http://download.oracle.com/javase/6/docs/api/java/net/URLConnection.html\" rel=\"noreferrer\"><code>URLConnection</code></a> instead. Furthermore you might want to leverage <a href=\"http://commons.apache.org/io/apidocs/org/apache/commons/io/IOUtils.html\" rel=\"noreferrer\"><code>IOUtils</code></a> from <a href=\"http://commons.apache.org/io/\" rel=\"noreferrer\">Apache Commons IO</a> to make the string reading easier too. For example:</p>");
       //System.out.println(answerBody);

       // String query = selectedText.replace(' ','+')+languageTag;
        //BrowserUtil.browse("https://stackoverflow.com/search?q="+query);

        formatted(editor,shown,"First answer from Stack Overflow");

    }
    public void update(AnActionEvent e){
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        CaretModel caretModel = editor.getCaretModel();
        e.getPresentation().setEnabledAndVisible(caretModel.getCurrentCaret().hasSelection());

    }
    static void formatted(Editor editor, String html, String title) {

        JBPopupFactory factory = JBPopupFactory.getInstance();

        BalloonBuilder builder =
                factory.createHtmlTextBalloonBuilder(html, MessageType.INFO, e -> {if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
                    BrowserUtil.browse(e.getURL());});
        builder.setTitle(title);

        builder.setFillColor(JBColor.background());

        builder.setBorderColor(JBColor.orange);

        Balloon b = builder.createBalloon();
        Point point = editor.logicalPositionToXY(editor.getCaretModel().getLogicalPosition());
        point.y += editor.getLineHeight();


        JComponent comp = editor.getContentComponent();
        Rectangle r = comp.getBounds();

        RelativePoint p = new RelativePoint(comp, point);
        b.show(p, Balloon.Position.below);

    }
}
