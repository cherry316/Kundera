package com.impetus.client.crud.datatypes;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import junit.framework.Assert;

import org.apache.cassandra.thrift.CfDef;
import org.apache.cassandra.thrift.ColumnDef;
import org.apache.cassandra.thrift.IndexType;
import org.apache.cassandra.thrift.InvalidRequestException;
import org.apache.cassandra.thrift.KsDef;
import org.apache.cassandra.thrift.NotFoundException;
import org.apache.cassandra.thrift.SchemaDisagreementException;
import org.apache.cassandra.thrift.TimedOutException;
import org.apache.cassandra.thrift.UnavailableException;
import org.apache.thrift.TException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.impetus.client.crud.datatypes.entities.StudentCassandraDate;
import com.impetus.client.persistence.CassandraCli;

public class StudentCassandraDateTest extends Base
{

    private static final String keyspace = "KunderaCassandraDataType";

    private EntityManagerFactory emf;

    @Before
    public void setUp() throws Exception
    {
        if (RUN_IN_EMBEDDED_MODE)
        {
            startCluster();
        }
        if (AUTO_MANAGE_SCHEMA)
        {
            createSchema();
        }
        emf = Persistence.createEntityManagerFactory("CassandraDataTypeTest");
    }

    @After
    public void tearDown() throws Exception
    {
        emf.close();
        if (AUTO_MANAGE_SCHEMA)
        {
            dropSchema();
        }
        if (RUN_IN_EMBEDDED_MODE)
        {
            stopCluster();
        }
    }

    @Test
    public void testExecuteUseSameEm()
    {
        testPersist(true);
        testFindById(true);
        testMerge(true);
        testFindByQuery(true);
        testNamedQueryUseSameEm(true);
        testDelete(true);
    }

    @Test
    public void testExecute()
    {
        testPersist(false);
        testFindById(false);
        testMerge(false);
        testFindByQuery(false);
        testNamedQuery(false);
        testDelete(false);
    }

    public void testPersist(boolean useSameEm)
    {
        EntityManager em = emf.createEntityManager();

        // Insert random value of Date
        StudentCassandraDate student = new StudentCassandraDate();
        student.setAge((Short) getRandomValue(short.class));
        student.setId((Date) getRandomValue(Date.class));
        student.setName((String) getRandomValue(String.class));
        em.persist(student);

        // Insert max value of Date
        StudentCassandraDate studentMax = new StudentCassandraDate();
        studentMax.setAge((Short) getMaxValue(short.class));
        studentMax.setId((Date) getMaxValue(Date.class));
        studentMax.setName((String) getMaxValue(String.class));
        em.persist(studentMax);

        // Insert min value of Date
        StudentCassandraDate studentMin = new StudentCassandraDate();
        studentMin.setAge((Short) getMinValue(short.class));
        studentMin.setId((Date) getMinValue(Date.class));
        studentMin.setName((String) getMinValue(String.class));
        em.persist(studentMin);

        em.close();
    }

    public void testFindById(boolean useSameEm)
    {
        EntityManager em = emf.createEntityManager();

        StudentCassandraDate studentMax = em.find(StudentCassandraDate.class, getMaxValue(Date.class));
        System.out.println(((Date) getMaxValue(Date.class)).getTime());
        Assert.assertNotNull(studentMax);
        Assert.assertEquals(getMaxValue(short.class), studentMax.getAge());
        Assert.assertEquals(getMaxValue(String.class), studentMax.getName());

        if (!useSameEm)
        {
            em.close();
            em = emf.createEntityManager();
        }
        StudentCassandraDate studentMin = em.find(StudentCassandraDate.class, getMinValue(Date.class));
        Assert.assertNotNull(studentMin);
        Assert.assertEquals(getMinValue(short.class), studentMin.getAge());
        Assert.assertEquals(getMinValue(String.class), studentMin.getName());

        if (!useSameEm)
        {
            em.close();
            em = emf.createEntityManager();
        }
        StudentCassandraDate student = em.find(StudentCassandraDate.class, getRandomValue(Date.class));
        Assert.assertNotNull(student);
        Assert.assertEquals(getRandomValue(short.class), student.getAge());
        Assert.assertEquals(getRandomValue(String.class), student.getName());
        em.close();
    }

    public void testMerge(boolean useSameEm)
    {
        EntityManager em = emf.createEntityManager();
        StudentCassandraDate student = em.find(StudentCassandraDate.class, getMaxValue(Date.class));
        Assert.assertNotNull(student);
        Assert.assertEquals(getMaxValue(short.class), student.getAge());
        Assert.assertEquals(getMaxValue(String.class), student.getName());
        System.out.println(student.getId().getTime());
        student.setName("Kuldeep");
        em.merge(student);
        if (!useSameEm)
        {
            em.close();
            em = emf.createEntityManager();
        }
        StudentCassandraDate newStudent = em.find(StudentCassandraDate.class, getMaxValue(Date.class));
        Assert.assertNotNull(newStudent);
        Assert.assertEquals(getMaxValue(short.class), newStudent.getAge());
        Assert.assertEquals("Kuldeep", newStudent.getName());
    }

    public void testFindByQuery(boolean useSameEm)
    {
        findAllQuery();
        findByName();
        findByAge();
        findByNameAndAgeGTAndLT();
        findByNameAndAgeGTEQAndLTEQ();
        findByNameAndAgeGTAndLTEQ();
        findByNameAndAgeWithOrClause();
        findByAgeAndNameGTAndLT();
        findByNameAndAGEBetween();
        // findByRange();
    }

    private void findByAgeAndNameGTAndLT()
    {
        EntityManager em;
        String query;
        Query q;
        List<StudentCassandraDate> students;
        int count;
        em = emf.createEntityManager();
        query = "Select s From StudentCassandraDate s where s.age = " + getMinValue(short.class)
                + " and s.name > Amresh and s.name <= " + getMaxValue(String.class);
        q = em.createQuery(query);
        students = q.getResultList();
        Assert.assertNotNull(students);
        Assert.assertEquals(1, students.size());
        count = 0;
        for (StudentCassandraDate student : students)
        {
            Assert.assertEquals(getMinValue(Date.class), student.getId());
            Assert.assertEquals(getMinValue(short.class), student.getAge());
            Assert.assertEquals(getMinValue(String.class), student.getName());
            count++;

        }
        Assert.assertEquals(1, count);
        em.close();

    }

    private void findByRange()
    {
        EntityManager em;
        String query;
        Query q;
        List<StudentCassandraDate> students;
        em = emf.createEntityManager();
        query = "Select s From StudentCassandraDate s where s.id between ?1  and ?2";
        q = em.createQuery(query);
        q.setParameter(1, getMinValue(Date.class));
        q.setParameter(2, getMaxValue(Date.class));
        students = q.getResultList();
        Assert.assertNotNull(students);
        Assert.assertEquals(3, students.size());
        int count = 0;
        for (StudentCassandraDate student : students)
        {
            if (student.getId().equals(getMaxValue(Date.class)))
            {
                Assert.assertEquals(getMaxValue(short.class), student.getAge());
                Assert.assertEquals("Kuldeep", student.getName());
                count++;
            }
            else if (student.getId().equals(getMinValue(Date.class)))
            {
                Assert.assertEquals(getMinValue(short.class), student.getAge());
                Assert.assertEquals(getMinValue(String.class), student.getName());
                count++;
            }
            else
            {
                Assert.assertEquals(getRandomValue(Date.class), student.getId());
                Assert.assertEquals(getRandomValue(short.class), student.getAge());
                Assert.assertEquals(getRandomValue(String.class), student.getName());
                count++;
            }
        }
        Assert.assertEquals(3, count);
        em.close();
    }

    private void findByNameAndAgeWithOrClause()
    {
        EntityManager em;
        String query;
        Query q;
        List<StudentCassandraDate> students;
        int count;
        em = emf.createEntityManager();
        query = "Select s From StudentCassandraDate s where s.name = Kuldeep and s.age > " + getMinValue(short.class);
        q = em.createQuery(query);
        students = q.getResultList();
        Assert.assertNotNull(students);
        Assert.assertEquals(1, students.size());
        count = 0;
        for (StudentCassandraDate student : students)
        {
            Assert.assertEquals(getMaxValue(Date.class), student.getId());
            Assert.assertEquals(getMaxValue(short.class), student.getAge());
            Assert.assertEquals("Kuldeep", student.getName());
            count++;
        }
        Assert.assertEquals(1, count);
        em.close();
    }

    private void findByNameAndAgeGTAndLTEQ()
    {

        EntityManager em;
        String query;
        Query q;
        List<StudentCassandraDate> students;
        int count;
        em = emf.createEntityManager();
        query = "Select s From StudentCassandraDate s where s.name = Kuldeep and s.age > " + getMinValue(short.class)
                + " and s.age <= " + getMaxValue(short.class);
        q = em.createQuery(query);
        students = q.getResultList();
        Assert.assertNotNull(students);
        Assert.assertEquals(1, students.size());
        count = 0;
        for (StudentCassandraDate student : students)
        {
            Assert.assertEquals(getMaxValue(Date.class), student.getId());
            Assert.assertEquals(getMaxValue(short.class), student.getAge());
            Assert.assertEquals("Kuldeep", student.getName());
            count++;
        }
        Assert.assertEquals(1, count);
        em.close();
    }

    public void testNamedQueryUseSameEm(boolean useSameEm)
    {
        updateNamed(true);
        deleteNamed(true);
    }

    public void testNamedQuery(boolean useSameEm)
    {
        updateNamed(false);
        deleteNamed(false);
    }

    public void testDelete(boolean useSameEm)
    {
        EntityManager em = emf.createEntityManager();

        StudentCassandraDate studentMax = em.find(StudentCassandraDate.class, getMaxValue(Date.class));
        Assert.assertNotNull(studentMax);
        Assert.assertEquals(getMaxValue(short.class), studentMax.getAge());
        Assert.assertEquals("Kuldeep", studentMax.getName());
        em.remove(studentMax);
        if (!useSameEm)
        {
            em.close();
            em = emf.createEntityManager();
        }
        studentMax = em.find(StudentCassandraDate.class, getMaxValue(Date.class));
        Assert.assertNull(studentMax);
        em.close();
    }

    /**
     * 
     */
    private void deleteNamed(boolean useSameEm)
    {

        String deleteQuery = "Delete From StudentCassandraDate s where s.name=Vivek";
        EntityManager em = emf.createEntityManager();
        Query q = em.createQuery(deleteQuery);
        q.executeUpdate();
        if (!useSameEm)
        {
            em.close();
            em = emf.createEntityManager();
        }
        StudentCassandraDate newStudent = em.find(StudentCassandraDate.class, getRandomValue(Date.class));
        Assert.assertNull(newStudent);
        em.close();
    }

    /**
     * @return
     */
    private void updateNamed(boolean useSameEm)
    {
        EntityManager em = emf.createEntityManager();
        String updateQuery = "Update StudentCassandraDate s SET s.name=Vivek where s.name=Amresh";
        Query q = em.createQuery(updateQuery);
        q.executeUpdate();
        if (!useSameEm)
        {
            em.close();
            em = emf.createEntityManager();
        }
        StudentCassandraDate newStudent = em.find(StudentCassandraDate.class, getRandomValue(Date.class));
        Assert.assertNotNull(newStudent);
        Assert.assertEquals(getRandomValue(short.class), newStudent.getAge());
        Assert.assertEquals("Vivek", newStudent.getName());
        em.close();
    }

    private void findByNameAndAGEBetween()
    {
        EntityManager em;
        String query;
        Query q;
        List<StudentCassandraDate> students;
        int count;
        em = emf.createEntityManager();
        query = "Select s From StudentCassandraDate s where s.name = Amresh and s.age between "
                + getMinValue(short.class) + " and " + getMaxValue(short.class);
        q = em.createQuery(query);
        students = q.getResultList();
        Assert.assertNotNull(students);
        Assert.assertEquals(1, students.size());
        count = 0;
        for (StudentCassandraDate student : students)
        {
            Assert.assertEquals(getRandomValue(Date.class), student.getId());
            Assert.assertEquals(getRandomValue(short.class), student.getAge());
            Assert.assertEquals(getRandomValue(String.class), student.getName());
            count++;

        }
        Assert.assertEquals(1, count);
        em.close();
    }

    private void findByNameAndAgeGTAndLT()
    {
        EntityManager em;
        String query;
        Query q;
        List<StudentCassandraDate> students;
        int count;
        em = emf.createEntityManager();
        query = "Select s From StudentCassandraDate s where s.name = Amresh and s.age > " + getMinValue(short.class)
                + " and s.age < " + getMaxValue(short.class);
        q = em.createQuery(query);
        students = q.getResultList();
        Assert.assertNotNull(students);
        Assert.assertEquals(1, students.size());
        count = 0;
        for (StudentCassandraDate student : students)
        {
            Assert.assertEquals(getRandomValue(Date.class), student.getId());
            Assert.assertEquals(getRandomValue(short.class), student.getAge());
            Assert.assertEquals(getRandomValue(String.class), student.getName());
            count++;

        }
        Assert.assertEquals(1, count);
        em.close();

    }

    private void findByNameAndAgeGTEQAndLTEQ()
    {
        EntityManager em;
        String query;
        Query q;
        List<StudentCassandraDate> students;
        int count;
        em = emf.createEntityManager();
        query = "Select s From StudentCassandraDate s where s.name = Kuldeep and s.age >= " + getMinValue(short.class)
                + " and s.age <= " + getMaxValue(short.class);
        q = em.createQuery(query);
        students = q.getResultList();
        Assert.assertNotNull(students);
        Assert.assertEquals(2, students.size());
        count = 0;
        for (StudentCassandraDate student : students)
        {
            if (student.getId().equals(getMaxValue(Date.class)))
            {
                Assert.assertEquals(getMaxValue(short.class), student.getAge());
                Assert.assertEquals("Kuldeep", student.getName());
                count++;
            }
            else
            {
                Assert.assertEquals(getMinValue(Date.class), student.getId());
                Assert.assertEquals(getMinValue(short.class), student.getAge());
                Assert.assertEquals(getMinValue(String.class), student.getName());
                count++;
            }

        }
        Assert.assertEquals(2, count);
        em.close();

    }

    private void findByAge()
    {
        EntityManager em;
        String query;
        Query q;
        List<StudentCassandraDate> students;
        int count;
        em = emf.createEntityManager();
        query = "Select s From StudentCassandraDate s where s.age = " + getRandomValue(short.class);
        q = em.createQuery(query);
        students = q.getResultList();
        Assert.assertNotNull(students);
        Assert.assertEquals(1, students.size());
        count = 0;
        for (StudentCassandraDate student : students)
        {
            Assert.assertEquals(getRandomValue(Date.class), student.getId());
            Assert.assertEquals(getRandomValue(short.class), student.getAge());
            Assert.assertEquals(getRandomValue(String.class), student.getName());
            count++;
        }
        Assert.assertEquals(1, count);
        em.close();
    }

    /**
     * 
     */
    private void findByName()
    {
        EntityManager em;
        String query;
        Query q;
        List<StudentCassandraDate> students;
        int count;
        em = emf.createEntityManager();
        query = "Select s From StudentCassandraDate s where s.name = Kuldeep";
        q = em.createQuery(query);
        students = q.getResultList();
        Assert.assertNotNull(students);
        Assert.assertEquals(2, students.size());
        count = 0;
        for (StudentCassandraDate student : students)
        {
            if (student.getId().equals(getMaxValue(Date.class)))
            {
                Assert.assertEquals(getMaxValue(short.class), student.getAge());
                Assert.assertEquals("Kuldeep", student.getName());
                count++;
            }
            else
            {
                Assert.assertEquals(getMinValue(Date.class), student.getId());
                Assert.assertEquals(getMinValue(short.class), student.getAge());
                Assert.assertEquals(getMinValue(String.class), student.getName());
                count++;
            }
        }
        Assert.assertEquals(2, count);
        em.close();
    }

    /**
     * 
     */
    private void findAllQuery()
    {
        EntityManager em = emf.createEntityManager();
        // Selet all query.
        String query = "Select s From StudentCassandraDate s ";
        Query q = em.createQuery(query);
        List<StudentCassandraDate> students = q.getResultList();
        Assert.assertNotNull(students);
        Assert.assertEquals(3, students.size());
        int count = 0;
        for (StudentCassandraDate student : students)
        {
            if (student.getId().equals(getMaxValue(Date.class)))
            {
                Assert.assertEquals(getMaxValue(short.class), student.getAge());
                Assert.assertEquals("Kuldeep", student.getName());
                count++;
            }
            else if (student.getId().equals(getMinValue(Date.class)))
            {
                Assert.assertEquals(getMinValue(short.class), student.getAge());
                Assert.assertEquals(getMinValue(String.class), student.getName());
                count++;
            }
            else
            {
                System.out.println(student.getId().getTime());
                Assert.assertEquals(((Date) getRandomValue(Date.class)).getTime(), student.getId().getTime());
                Assert.assertEquals(getRandomValue(short.class), student.getAge());
                Assert.assertEquals(getRandomValue(String.class), student.getName());
                count++;
            }
        }
        Assert.assertEquals(3, count);
        em.close();
    }

    public void startCluster()
    {
        try
        {
            CassandraCli.cassandraSetUp();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (TException e)
        {
            e.printStackTrace();
        }
        catch (InvalidRequestException e)
        {
            e.printStackTrace();
        }
        catch (UnavailableException e)
        {
            e.printStackTrace();
        }
        catch (TimedOutException e)
        {
            e.printStackTrace();
        }
        catch (SchemaDisagreementException e)
        {
            e.printStackTrace();
        }
    }

    public void stopCluster()
    {
        // TODO Auto-generated method stub

    }

    public void createSchema()
    {
        try
        {
            KsDef ksDef = null;

            CfDef cfDef = new CfDef();
            cfDef.name = "StudentCassandraDate";
            cfDef.keyspace = keyspace;
            cfDef.setKey_validation_class("DateType");

            ColumnDef name = new ColumnDef(ByteBuffer.wrap("NAME".getBytes()), "UTF8Type");
            name.index_type = IndexType.KEYS;
            cfDef.addToColumn_metadata(name);
            ColumnDef age = new ColumnDef(ByteBuffer.wrap("AGE".getBytes()), "IntegerType");
            age.index_type = IndexType.KEYS;
            cfDef.addToColumn_metadata(age);
            List<CfDef> cfDefs = new ArrayList<CfDef>();
            cfDefs.add(cfDef);
            try
            {
                CassandraCli.initClient();
                ksDef = CassandraCli.client.describe_keyspace(keyspace);
                CassandraCli.client.set_keyspace(keyspace);

                List<CfDef> cfDefn = ksDef.getCf_defs();

                for (CfDef cfDef1 : cfDefn)
                {

                    if (cfDef1.getName().equalsIgnoreCase("StudentCassandraDate"))
                    {

                        CassandraCli.client.system_drop_column_family("StudentCassandraDate");

                    }
                }
                CassandraCli.client.system_add_column_family(cfDef);

            }
            catch (NotFoundException e)
            {

                ksDef = new KsDef(keyspace, "org.apache.cassandra.locator.SimpleStrategy", cfDefs);
                // Set replication factor
                if (ksDef.strategy_options == null)
                {
                    ksDef.strategy_options = new LinkedHashMap<String, String>();
                }
                // Set replication factor, the value MUST be an int
                ksDef.strategy_options.put("replication_factor", "1");
                CassandraCli.client.system_add_keyspace(ksDef);
            }

            CassandraCli.client.set_keyspace(keyspace);
        }
        catch (TException e)
        {
            e.printStackTrace();
        }
        catch (InvalidRequestException e)
        {
            e.printStackTrace();
        }
        catch (SchemaDisagreementException e)
        {
            e.printStackTrace();
        }

    }

    public void dropSchema()
    {
        try
        {
            CassandraCli.client.system_drop_keyspace(keyspace);
        }
        catch (InvalidRequestException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (SchemaDisagreementException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (TException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}