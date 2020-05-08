package application.element;

import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextArea;

/**
 * @author qdyk
 * @title: CustomCell
 * @projectName KafkaUI
 * @description: TODO
 * @date 2020/4/1716:10
 */
public class TextAreaCell<T, S> extends TableCell<T, S> {
    /**
     * 详细信息框, 用于显示列的详细信息
     */
    private TextArea textArea;

    /**
     * 构造方法
     * @param textArea
     */
    public TextAreaCell(TextArea textArea) {
        this.textArea = textArea;
    }

    /**
     * 更新字段信息
     * @param item
     * @param empty
     */
    @Override
    public void updateItem(S item, boolean empty) {
        onMouseClicked(item);
        if (item == getItem()) return;

        super.updateItem(item, empty);

        if (item == null) {
            super.setText(null);
            super.setGraphic(null);
        } else if (item instanceof Node) {
            super.setText(null);
            super.setGraphic((Node) item);
        } else {
            super.setText(item.toString());
            super.setGraphic(null);
        }
    }

    /**
     * 设置单击单元格显示详细内容
     * @param item
     */
    void onMouseClicked(S item){
        setOnMouseClicked(e->textArea.setText(item.toString()));
    }
}
