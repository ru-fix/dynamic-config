package ru.fix.dynamic.config.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;

import java.util.Collection;
import java.util.Optional;

/**
 * @author Kamil Asfandiyarov
 */
public class DynamicConfigAction extends AnAction {

    static final String ZkConfigClass = "ru.fix.commons.zkconfig.api.ZkConfig";

    static Logger log = Logger.getInstance(DynamicConfigAction.class);

    /**
     * Ask user with question box for package prefix,
     * search all ZkConfig fields within that package
     * generate new DynamicProperties declaration
     * convert old ZkConfig fields to new one
     *
     * @param event
     */
    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        Editor editor = event.getData(PlatformDataKeys.EDITOR);

        if (editor == null) {
            log.error("Editor is not active. Put cursor where you want to insert generated code.");
            return;
        }

        ProjectAndLibrariesScope searchScope = new ProjectAndLibrariesScope(project);

        PsiClass zkAnnocation = JavaPsiFacade.getInstance(project).findClass(ZkConfigClass, searchScope);

        if (zkAnnocation == null) {
            log.error("Can not found psi class for annotation: " + ZkConfigClass);
            return;
        }

        Collection<PsiField> fields = AnnotatedElementsSearch.searchPsiFields(zkAnnocation, searchScope).findAll();

        String packagePrefix = Messages.showInputDialog(project,
                "Enter package prefix, e.g. ru.fix.cpapsm.subscription.web.server",
                "Package prefix to scan",
                Messages.getQuestionIcon());


        StringBuilder generatedCode = new StringBuilder();

        fields.stream()
                .filter(field -> Optional.ofNullable(field.getContainingClass())
                        .map(PsiClass::getQualifiedName)
                        .map(prefix -> prefix.startsWith(packagePrefix)).orElse(Boolean.FALSE))
                .forEach(field -> {
                    FieldInfo fieldInfo = analyseField(field);

                    generatedCode.append(generatePropertyDescriptionCodeBlock(fieldInfo));
                    updateOldDefenition(project, field, fieldInfo);
                });


        Document document = editor.getDocument();
        int offset = editor.getCaretModel().getOffset();

        ApplicationManager.getApplication().invokeLater(() -> {
            ApplicationManager.getApplication().runReadAction(() -> {
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    document.insertString(offset, generatedCode.toString());
                });
            });
        });
    }

    /**
     * @return String representation of field type e.g. java.lang.Integer
     */
    static String extractFieldType(PsiField field) {
        String code = field.getType().getCanonicalText();
        return code.substring(code.indexOf("<") + 1, code.indexOf(">"));
    }

    static class FieldInfo {
        public String containingClass;
        public String zkName;
        public String zkDescription;
        public String zkDefaultValue;
        public String fieldType;
        public String fieldName;
    }

    static FieldInfo analyseField(PsiField field) {
        FieldInfo result = new FieldInfo();
        result.containingClass = field.getContainingClass().getQualifiedName();
        result.zkName = extractAnnotationAttribute(field, "name");
        result.zkDescription = extractAnnotationAttribute(field, "description");
        result.zkDefaultValue = extractAnnotationAttribute(field, "defaultValue");
        result.fieldType = extractFieldType(field);
        result.fieldName = field.getName();

        return result;
    }


    /**
     * @param field
     * @return block of code with property description and default value initialization
     */
    static String generatePropertyDescriptionCodeBlock(FieldInfo field) {

        try {
            Class<?> checkClass = Class.forName(field.fieldType);
            if (Number.class.isAssignableFrom(checkClass) || Boolean.class.isAssignableFrom(checkClass)) {
                field.zkDefaultValue = field.zkDefaultValue.replace("\"", "");
                if (checkClass.isAssignableFrom(Long.class)) {
                    field.zkDefaultValue = field.zkDefaultValue + "L";
                }
            }
            if (field.fieldType.contains("java.lang")) {
                field.fieldType = checkClass.getSimpleName();
            }
        } catch (ClassNotFoundException e) {
            log.info(e);
        }


        return String.format(
                "//class: %s\n" +
                        "@DynamicPropertyDescription(id=%s,\ndescription=%s)\n" +
                        "%s %s = %s;\n\n",
                field.containingClass,
                field.zkName,
                field.zkDescription,
                field.fieldType,
                field.zkName
                        .replace('.', '_')
                        .replace("\"", "") + "_" + field.fieldName,
                field.zkDefaultValue
        );

    }


    /**
     * @param field
     * @param name  attribute name
     * @return literal representation of annotation argument
     */
    static String extractAnnotationAttribute(PsiField field, String name) {
        PsiNameValuePair[] attributes = field.getModifierList().getAnnotations()[0].getParameterList().getAttributes();
        for (PsiNameValuePair attribute : attributes) {
            if (attribute != null && attribute.getName() != null && attribute.getName().equals(name)) {
                if (attribute.getValue() != null) {
                    PsiReference reference = attribute.getValue().getReference();
                    if (reference != null) {
                        return resolveAndGetReferences(reference.resolve());
                    }
                    PsiElement[] children = attribute.getValue().getChildren();
                    StringBuilder refs = null;

                    PsiElement firstChild = children[0];
                    if (firstChild.getReference() != null) {
                        refs = new StringBuilder(resolveAndGetReferences(firstChild.getReference().resolve())
                                .replaceAll("\"", ""));
                        for (int index = 1; index < children.length; index++) {
                            PsiReference refPart = children[index].getReference();
                            if (refPart != null) {
                                refs.append(resolveAndGetReferences(refPart.resolve()).replaceAll("\"", ""));
                            }
                        }
                    }
                    if (refs != null) {
                        return "\"" + refs.toString() + "\"";
                    }
                }
                return attribute.getValue().getText();

            }
        }
        return "";
    }

    private static String resolveAndGetReferences(PsiElement element) {
        PsiElement[] children = element.getChildren();
        for (PsiElement child : children) {
            if (child instanceof PsiLiteralExpression) {
                return child.getText();
            }
        }
        return "";
    }

    static void updateOldDefenition(Project project, PsiField field, FieldInfo fieldInfo) {
        ApplicationManager.getApplication().invokeLater(() -> {
            ApplicationManager.getApplication().runReadAction(() -> {
                WriteCommandAction.runWriteCommandAction(project, () -> {

                    field.getModifierList().getAnnotations()[0].delete();

                    PsiAnnotation annotation = JavaPsiFacade.getInstance(project)
                            .getElementFactory()
                            .createAnnotationFromText("@ru.fix.dynamic.config.spring.annotation.DynamicProperty(" + fieldInfo.zkName + ")", null);

                    JavaCodeStyleManager javaCodeStyleManager = JavaCodeStyleManager.getInstance(project);

                    field.getModifierList().addAfter(javaCodeStyleManager.shortenClassReferences(annotation),
                            null);
                    javaCodeStyleManager.removeRedundantImports((PsiJavaFile) field.getContainingFile());
                });
            });

        });

    }
}
