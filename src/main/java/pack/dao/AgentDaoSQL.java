package pack.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import pack.HibernateSessionFactory;
import pack.model.Agent;

import javax.transaction.Transactional;
import java.util.List;

public class AgentDaoSQL implements AgentDao {

    private SessionFactory sessionFactory;

    public AgentDaoSQL() {
        sessionFactory = HibernateSessionFactory.getSessionFactory();
    }

    @Override
    @Transactional
    public void add(Agent agent) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.persist(agent);
        session.getTransaction().commit();
        session.close();
    }

    @Override
    @Transactional
    public Agent read(Integer id) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        Agent agent = session.get(Agent.class,id);
        session.getTransaction().commit();
        session.close();
        return agent;
    }

    @Override
    @Transactional
    public void update(Agent agent) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.update(agent);
        session.getTransaction().commit();
        session.close();
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        Agent agent = session.get(Agent.class,id);
        if (agent !=null){
            session.delete(agent);
        }
        session.getTransaction().commit();
        session.close();
    }

    @Override
    @Transactional
    public List<Agent> readAll() {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        List<Agent> agents = session.createQuery("from Agent").list();
        session.getTransaction().commit();
        session.close();
        return agents;
    }

    @Override
    public Agent findByField(String field, String text) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        List<Agent> agents = session.createQuery("from Agent").list();
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
        session.getTransaction().commit();
        session.close();
        return a;
    }
}
