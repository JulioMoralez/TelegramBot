package pack.dao;

import pack.model.Agent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AgentDaoMemory implements AgentDao {

    private List<Agent> agents = new ArrayList<>();
    private static Integer currentId=1;

    @Override
    public void add(Agent t) {
        t.setId(currentId++);
        agents.add(t);
    }

    @Override
    public Agent read(Integer id) {
        return agents.get(id);
    }

    @Override
    public void update(Agent t) {
        agents.set(agents.indexOf(t),t);
    }

    @Override
    public void delete(Integer id) {

    }

    @Override
    public List<Agent> readAll() {
        return agents;
    }

    @Override
    public Agent findByField(String field, String text) {
        Agent a=null;
        for (Agent agent: agents){
            if (field.equals("nick")){
                if (agent.getNick().equalsIgnoreCase(text)){
                    a=agent;
                    break;
                }
            }
            if (field.equals("telegram")){
                if (agent.getTelegram().equalsIgnoreCase(text)){
                    a=agent;
                    break;
                }
            }
        }
        return a;
    }
}
