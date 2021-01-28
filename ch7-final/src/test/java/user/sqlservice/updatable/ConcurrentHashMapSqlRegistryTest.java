package user.sqlservice.updatable;

public class ConcurrentHashMapSqlRegistryTest extends ApstractUpdatableSqlRegistryTest {

    protected UpdatableSqlRegistry createUpdatableSqlRegistry() {
        return new ConcurrentHashMapSqlRegistry();
    }
}
