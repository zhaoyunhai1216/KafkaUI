package application.utils;

import application.element.TextAreaCell;
import application.element.EditingCell;
import application.element.Row;
import com.alibaba.fastjson.JSONObject;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;

import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author qdyk
 * @title: UtilCommons
 * @projectName KafkaUI
 * @description: TODO
 * @date 2020/3/2116:47
 */
public class UtilCommons {
    /**
     * 打开一个新的窗口方法
     *
     * @param fxml
     * @return
     */
    public static FXMLLoader newFXMLLoader(String fxml) {
        URL location = UtilCommons.class.getResource(fxml);
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
        return fxmlLoader;
    }

    /**
     * JSON转换为行信息
     */
    public static Row toRow(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        Row row = new Row();
        for (String key : jsonObject.keySet()) {
            row.put(key, new SimpleObjectProperty(jsonObject.get(key)));
        }
        return row;
    }

    /**
     * JSON转换为行信息
     */
    public static Row toRow(String jsonString) {
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        if (jsonObject == null) {
            return null;
        }
        return toRow(jsonObject);
    }

    /**
     * 获取随机一个节点
     *
     * @param hosts
     * @return
     */
    public static InetSocketAddress leastLoadedNode(String hosts) {
        List<InetSocketAddress> nodes = new ArrayList<>();
        String[] n = hosts.split(",");
        for (int i = 0; i < n.length; i++) {
            String[] net = n[i].split("\\:");
            nodes.add(new InetSocketAddress(net[0], Integer.parseInt(net[1])));
        }
        return nodes.get(new Random().nextInt(nodes.size()));
    }


    /**
     * 绑定组件大小
     */
    public static Pane bindSize(Pane src, Pane dst) {
        src.prefWidthProperty().bind(dst.widthProperty());
        src.prefHeightProperty().bind(dst.heightProperty());
        return src;
    }

    /**
     * 绑定组件大小
     */
    public static Pane bindWidth(Pane src, Pane dst) {
        src.prefWidthProperty().bind(dst.widthProperty());
        return src;
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    public static void initEditTableColumn(TableView<Row> tableView) {
        List<TableColumn<Row, ?>> columns = tableView.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            TableColumn<Row, ?> tableColumn = columns.get(i);
            tableColumn.setCellValueFactory(x -> x.getValue().get(tableColumn.getText()));
            tableColumn.setCellFactory(p -> new EditingCell<>());
        }
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    public static void initReadonlyColumns(TableView<Row> tableView) {
        tableView.getColumns().forEach(column -> {
            column.setCellValueFactory(x -> x.getValue().get(column.getText()));
        });
    }
    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    public static void initTextAreaColumns(TableView<Row> tableView, TextArea textArea) {
        tableView.getColumns().forEach(column -> {
            column.setCellValueFactory(x -> x.getValue().get(column.getText()));
            column.setCellFactory(p -> new TextAreaCell<>(textArea));
        });
    }

}
