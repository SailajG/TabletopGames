package players.RL;

import core.AbstractGameState;
import core.AbstractPlayer;
import core.actions.AbstractAction;
import core.components.PartialObservableDeck;
import games.loveletter.LoveLetterForwardModel;
import games.loveletter.LoveLetterGameState;
import games.loveletter.cards.LoveLetterCard;

import java.util.*;

public class QL extends AbstractPlayer {

    Map<String, Double> qValue = new HashMap<>();
    List<String> trajectory = new ArrayList<>();
    Random rnd = new Random();
    LoveLetterForwardModel fm = new LoveLetterForwardModel();


    @Override
    public AbstractAction getAction(AbstractGameState gameState, List<AbstractAction> possibleActions) {

        double randomValue;
        double epsilon;
        double bestValue = Double.NEGATIVE_INFINITY;
        AbstractAction bestAction = null;
        randomValue = rnd.nextDouble();
        String key = "";
        double value;
        for (epsilon = 0.9; epsilon >= 0.1; epsilon = epsilon - 0.1) {
            if (randomValue < epsilon) {
                int randomAction = rnd.nextInt(possibleActions.size());
                AbstractAction a = possibleActions.get(randomAction);
                key = psi((LoveLetterGameState) gameState, a);
                value = qValue.getOrDefault(key, 0.0);
                if (value > bestValue) {
                    bestAction = a;
                    bestValue = value;
                }
            } else {
                for (AbstractAction b : possibleActions) {
                    key = psi((LoveLetterGameState) gameState, b);
                    value = qValue.getOrDefault(key, 0.0);
                    if (value > bestValue) {
                        bestAction = b;
                        bestValue = value;
                    }
                }
            }
        }
        trajectory.add(key);
        return bestAction;
    }


    private String psi(LoveLetterGameState state, AbstractAction action) {

        PartialObservableDeck<LoveLetterCard> hand = state.getPlayerHandCards().get(state.getCurrentPlayer());
        String card = "";
        if (hand.getSize() == 2) {
            String cardOne = hand.get(0).cardType.name();
            String cardTwo = hand.get(1).cardType.name();
            String actions = action.toString();
            card = cardOne + cardTwo + actions;
        } else if (hand.getSize() == 1) {
            String cardOne = hand.get(0).cardType.name();
            String actions = action.toString();
            card = cardOne + actions;
        } else if (hand.getSize() == 0) {
            String actions = action.toString();
            card = actions;
        }
        return card;
    }

    @Override
    public void initializePlayer(AbstractGameState gameState) {
        qValue = new HashMap<>();
        rnd = new Random();
        trajectory = new ArrayList<>();
        fm = new LoveLetterForwardModel();
    }

    @Override
    public void finalizePlayer(AbstractGameState gameState) {

        double gamma=0.9, alpha=0.1;
        for (int i = trajectory.size() - 1; i >= 0; i--) {
            String key = trajectory.get(i);
            double targetValue;
            double reward;
            double oldValue;
            double newValue;
            if (i == trajectory.size() - 1) {
                reward = gameState.getGameScore(gameState.getCurrentPlayer());
                oldValue = 0.0;
            } else {
                reward = 0.0;
                oldValue = qValue.getOrDefault(key, 0.0);
            }
            targetValue = reward + (gamma * oldValue);
            newValue = oldValue + alpha * (targetValue - oldValue);
            qValue.put(key, newValue);
        }
    }


    @Override
    public AbstractPlayer copy() {
        return this;
    }

}