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
package jgnash.uifx.views.register;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import jgnash.engine.Engine;
import jgnash.engine.EngineFactory;
import jgnash.engine.InvestmentTransaction;
import jgnash.engine.Transaction;
import jgnash.uifx.StaticUIMethods;
import jgnash.uifx.views.accounts.SelectAccountSecuritiesDialog;
import jgnash.util.NotNull;

/**
 * Investment Register pane controller
 *
 * @author Craig Cavanaugh
 */
public class InvestmentRegisterPaneController extends RegisterPaneController {

    @FXML
    private ComboBox<SlipControllerContainer> actionComboBox;

    @FXML
    private StackPane transactionSlips;

    @FXML
    @Override
    public void initialize() {
        super.initialize();

        actionComboBox.setEditable(false);

        // Load the register table
        try {
            final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("InvestmentRegisterTable.fxml"), resources);
            registerTablePane.getChildren().add(fxmlLoader.load());
            registerTableControllerProperty.setValue(fxmlLoader.getController());
        } catch (final IOException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getLocalizedMessage(), e);
        }

        getAccountProperty().addListener((observable, oldValue, newValue) -> {
            buildTabs();
        });

        actionComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                oldValue.getController().clearForm();
            }
            transactionSlips.getChildren().clear();
            transactionSlips.getChildren().addAll(newValue.getPane());
        });
    }

    private void buildTabs() {
        final String[] actions = new String[]{resources.getString("Transaction.BuyShare"),
                resources.getString("Transaction.SellShare"), resources.getString("Transaction.TransferIn"),
                resources.getString("Transaction.TransferOut"), resources.getString("Transaction.AddShare"),
                resources.getString("Transaction.RemoveShare"), resources.getString("Transaction.ReinvestDiv"),
                resources.getString("Transaction.Dividend"), resources.getString("Transaction.SplitShare"),
                resources.getString("Transaction.MergeShare"), resources.getString("Transaction.ReturnOfCapital")};

        final List<SlipControllerContainer> transactionPanes = new ArrayList<>();

        // TODO: more investment slips
        transactionPanes.add(buildCashTransferTab(actions[2], SlipType.INCREASE));
        transactionPanes.add(buildCashTransferTab(actions[3], SlipType.DECREASE));

        actionComboBox.getItems().addAll(transactionPanes);

        actionComboBox.getSelectionModel().select(0);    // force selection
    }

    private SlipControllerContainer buildCashTransferTab(final String name, final SlipType slipType) {

        try {
            final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("InvestmentTransactionPane.fxml"), resources);
            final Pane pane = fxmlLoader.load();

            final SlipController slipController = fxmlLoader.getController();

            slipController.setSlipType(slipType);
            slipController.getAccountProperty().bind(getAccountProperty());

            return new SlipControllerContainer(name, slipController, pane);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void handleEnterAction() {
        actionComboBox.getSelectionModel().selectedItemProperty().get().getController().handleEnterAction();
    }

    @FXML
    private void handleCancelAction() {
        actionComboBox.getSelectionModel().selectedItemProperty().get().getController().handleCancelAction();
    }

    @FXML
    private void handleSecuritiesAction() {
        final SelectAccountSecuritiesDialog dialog = new SelectAccountSecuritiesDialog(getAccountProperty().get(),
                getAccountProperty().get().getSecurities());

        if (dialog.showAndWait()) {
            final Engine engine = EngineFactory.getEngine(EngineFactory.DEFAULT);
            Objects.requireNonNull(engine);

            engine.updateAccountSecurities(getAccountProperty().get(), dialog.getSelectedSecurities());
        }
    }

    @Override
    protected void modifyTransaction(@NotNull final Transaction transaction) {
        if (transaction.areAccountsLocked()) {
            StaticUIMethods.displayError(resources.getString("Message.TransactionModifyLocked"));
            return;
        }

        if (transaction instanceof InvestmentTransaction) {
            switch (transaction.getTransactionType()) {
                default:
                    // TODO: more investment slips
                    break;
            }
        } else {
            if (transaction.getAmount(getAccountProperty().get()).signum() >= 0) {
                actionComboBox.getSelectionModel().select(2);  // transferIn
            } else {
                actionComboBox.getSelectionModel().select(3);  // transferOut
            }
            actionComboBox.getSelectionModel().getSelectedItem().getController().modifyTransaction(transaction);
        }
    }
}
