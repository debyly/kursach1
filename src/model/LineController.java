package model;

import java.util.HashMap;

import static model.LVS.LineState.*;
import static model.TerminalDevice.DeviceState.*;
import static model.TimeCounter.TimeType.*;

public class LineController {
    private TimeCounter timer = new TimeCounter();
    private HashMap<Integer, TerminalDevice> clients;
    private LVS.Line line;
    private int messageCount = 0;

    LineController(HashMap<Integer, TerminalDevice> clients, LVS.Line line){

        this.clients = clients;
        this.line = line;
    }

    public int getTime(){
        return timer.getTime();
    }

    int getMessageCount() {
        return messageCount;
    }

    //======= Действия при определенных неполадках ============
    void reactOn(TerminalDevice td){
        switch (td.state){
            // Абонент занят
            case BUSY:
                busy();
                td.restore();
                break;
            // Сбой
            case FAILURE:
                failure();
                td.restore();
                break;
            // Отказ или блокировка ОУ
            case DENIAL:
            case BLOCKED:
                denial();
                break;
            //Нормальная работа
            default:
                normalWork();
                break;
        }
    }

    private void failure(){
        timer.addTime(COMMAND);
        timer.addTime(WORD);
        timer.addTime(PAUSE_BEFORE_ANSWER);
        messageCount += 13;
    }

    private void normalWork(){
        timer.addTime(COMMAND);
        timer.addTime(WORD);
        timer.addTime(PAUSE_BEFORE_ANSWER);
        timer.addTime(ANSWER);
        messageCount += 14;
        line.setState(A_WORKING);
    }

    private void denial(){
        for (int i = 0; i < 2; i++){
            timer.addTime(COMMAND);
            timer.addTime(WORD);
            timer.addTime(PAUSE_BEFORE_ANSWER);
            messageCount += 13;
        }
        line.setState(B_WORKING);
    }

    private void busy(){
        timer.addTime(COMMAND);
        timer.addTime(WORD);
        timer.addTime(PAUSE_BEFORE_ANSWER);
        timer.addTime(ANSWER);
        timer.addTime(PAUSE_IF_BUSY);
        messageCount += 14;
    }

    void findGenerator(){

        // ================ Тест МКО ====================
        for(int i = 0; i < 18; i++){
            timer.addTime(COMMAND);
            timer.addTime(PAUSE_BEFORE_ANSWER);

            messageCount += 1;
        }
        //================================================

        //=============== Блокировка всех ОУ =============
        line.setState(B_WORKING);
            for(int i = 0; i < 18; i++) {

                timer.addTime(BLOCK);
                timer.addTime(PAUSE_BEFORE_ANSWER);
                timer.addTime(ANSWER);

                clients.get(i).chState(BLOCKED);

                messageCount += 2;
            }
        //==================================================

            int i = 0;
            do{

                line.setState(B_WORKING);

                //======= Разблокировка одного ОУ ==========
                timer.addTime(UNBLOCK);
                timer.addTime(PAUSE_BEFORE_ANSWER);
                timer.addTime(ANSWER);

                clients.get(i).chState(WORKING);

                messageCount += 2;
                //===========================================

                line.setState(A_WORKING);

                //============= Опрос текущего ОУ =================
                timer.addTime(COMMAND);
                timer.addTime(PAUSE_BEFORE_ANSWER);
                messageCount += 1;

                if(!(clients.get(i).state == GENERATOR)) {
                    timer.addTime(ANSWER);
                    messageCount += 1;
                }
                //==================================================

                else {
                    //======== Опрос предыдущего ОУ ==========
                    timer.addTime(COMMAND);
                    timer.addTime(PAUSE_BEFORE_ANSWER);
                    messageCount += 1;
                    //========================================
                    line.setState(B_WORKING);
                    //====== Блокируем генерящий элемент ======
                    timer.addTime(BLOCK);
                    timer.addTime(PAUSE_BEFORE_ANSWER);
                    timer.addTime(ANSWER);
                    clients.get(i).chState(BLOCKED);
                    messageCount += 2;
                    //===================================================================
                    //======= Остановка после обнаружения генерящего элемента ===========
                    break;
                }
            } while(true);
            //===== Разблокировка ОУ после генерящего =====
            i++;
            for(; i< 18; i++ ){
                timer.addTime(UNBLOCK);
                timer.addTime(PAUSE_BEFORE_ANSWER);
                timer.addTime(ANSWER);
                clients.get(i).chState(WORKING);
                messageCount += 2;
            //==============================================
            }
        line.setState(A_WORKING);
    }
}