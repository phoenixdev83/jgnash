/*
 * jGnash, a personal finance application
 * Copyright (C) 2001-2015 Craig Cavanaugh
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jgnash.uifx.control;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import jgnash.engine.Engine;
import jgnash.engine.EngineFactory;
import jgnash.engine.SecurityNode;
import jgnash.engine.message.Message;
import jgnash.engine.message.MessageBus;
import jgnash.engine.message.MessageChannel;
import jgnash.engine.message.MessageListener;
import jgnash.engine.message.MessageProperty;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * ComboBox that allows selection of a SecurityNode and manages it's own model
 *
 * @author Craig Cavanaugh
 */
public class SecurityComboBox extends ComboBox<SecurityNode> implements MessageListener{

    /** Model for the ComboBox */
    private ObservableList<SecurityNode> items;

    public SecurityComboBox() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("SecurityComboBox.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }

        Platform.runLater(this::loadModel); // lazy load to let the ui build happen faster
    }

    private void loadModel() {
        final Engine engine = EngineFactory.getEngine(EngineFactory.DEFAULT);
        Objects.requireNonNull(engine);

        final List<SecurityNode> nodeList = engine.getSecurities();

        System.out.println(nodeList.size());

        // extract and reuse the default model
        items = getItems();

        // warp in a sorted list
        setItems(new SortedList<>(items, null));

        if (!nodeList.isEmpty()) {
            items.addAll(nodeList);
            getSelectionModel().select(0);
        }

        MessageBus.getInstance().registerListener(this, MessageChannel.COMMODITY, MessageChannel.SYSTEM);
    }

    @Override
    public void messagePosted(final Message event) {
        if (event.getObject(MessageProperty.COMMODITY) instanceof SecurityNode) {

            final SecurityNode node = (SecurityNode) event.getObject(MessageProperty.COMMODITY);

            Platform.runLater(() -> {
                switch (event.getEvent()) {
                    case SECURITY_REMOVE:
                        items.removeAll(node);
                        break;
                    case SECURITY_ADD:
                        items.add(node);
                        break;
                    case SECURITY_MODIFY:
                        items.removeAll(node);
                        items.add(node);
                        break;
                    case FILE_CLOSING:
                        items.clear();
                    default:
                        break;
                }
            });
        }
    }
}
