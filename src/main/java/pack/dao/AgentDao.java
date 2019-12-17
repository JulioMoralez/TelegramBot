package pack.dao;

import pack.model.Agent;

import java.util.List;

public interface AgentDao {
    void add(Agent t);
    Agent read(Integer id);
    void update(Agent t);
    void delete(Integer id);
    List<Agent> readAll();
    Agent findByField(String field, String text);
}