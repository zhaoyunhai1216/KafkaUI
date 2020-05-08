package application.element;

import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;

/**
 * @author zhaoyunhai
 * @title: EditingCell
 * @projectName KafkaUI
 * @description: TODO
 * @date 2020/3/1915:26
 */
public class EditingCell<T,S> extends TableCell<T, S> {

    private TextField textField;

    @Override
    public void startEdit() {
        if (!isEmpty()) {
            super.startEdit();
            createTextField();
            setText(null);
            setGraphic(textField);
            textField.selectAll();
        }
    }


    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(getItem().toString());
        setGraphic(null);
    }


    @Override
    public void updateItem(S item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (textField != null) {
                    textField.setText(getString());
                }
                setText(null);
                setGraphic(textField);
            } else {
                setText(getString());
                setGraphic(null);
            }
        }
    }


    private void createTextField() {
        textField = new TextField(getString());
        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
        textField.focusedProperty().addListener((ob, old, now) -> {
            if (!now) {
                commitEdit((S)textField.getText());
            }
        });
    }


    private String getString() {
        return getItem() == null ? "" : getItem().toString();
    }
}
