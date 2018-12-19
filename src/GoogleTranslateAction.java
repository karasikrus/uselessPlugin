import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;

public class GoogleTranslateAction extends AnAction {

    private static String splitCamelCase(String s) {
        return s.replaceAll(
                String.format("%s|%s|%s",
                        "(?<=[A-Z])(?=[A-Z][a-z])",
                        "(?<=[^A-Z])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z])"
                ),
                " "
        );
    }

    @Override
    public void actionPerformed(AnActionEvent e) {




            Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
            CaretModel caretModel = editor.getCaretModel();
            String selectedText = caretModel.getCurrentCaret().getSelectedText();
            String query = splitCamelCase(selectedText);
            BrowserUtil.browse("https://translate.google.com/?hl=ru#view=home&op=translate&sl=en&tl=ru&text="+query);

    }
        public void update(AnActionEvent e){
            Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
            CaretModel caretModel = editor.getCaretModel();
            e.getPresentation().setEnabledAndVisible(caretModel.getCurrentCaret().hasSelection());

        }
    }

