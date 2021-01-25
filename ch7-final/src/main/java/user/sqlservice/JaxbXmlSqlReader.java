package user.sqlservice;

import user.dao.UserDao;
import user.sqlservice.jaxb.SqlType;
import user.sqlservice.jaxb.Sqlmap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;

public class JaxbXmlSqlReader implements SqlReader {

    private static final String DEFAULT_SQLMAP_FILE = "/sqlmap.xml";                    // 굳이 상수로 만들지 않고 바로 sqlmapFile의 값으로 넣어도 상관없지만 이렇게 해주면 의도가 코드에 분명히 드러나고 코드도 폼이 난다.
    private String sqlmapFile = DEFAULT_SQLMAP_FILE;

    public void setSqlmapFile(String sqlmapFile) {
        this.sqlmapFile = sqlmapFile;                                                // sqlMapFile은 SqlReader의 특정 구현 방법에 종속되는 프로퍼티가 된다.
    }

    @Override
    public void read(SqlRegistry sqlRegistry) {                                        // loadSql()에 있던 코드를 SqlReader메소드로 가져온다. 초기화를 위해 무엇을 할 것인가와 SQL을 어떻게 읽는지를 분리한다.
        // UserDao와 같은 클래스패스의 sqlmap.xml 파일을 변환한다.
        String contextPath = Sqlmap.class.getPackage().getName();
        try {
            JAXBContext context = JAXBContext.newInstance(contextPath);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            InputStream is = UserDao.class.getResourceAsStream(this.sqlmapFile);
            Sqlmap sqlmap = (Sqlmap) unmarshaller.unmarshal(is);
            for (SqlType sql : sqlmap.getSql()) {
                sqlRegistry.registerSql(sql.getKey(), sql.getValue());                // SQL 저장 로직 구현에 독립적인 인터페이스 메소드를 통해 읽어들인 SQL과 키를 전달한다.
            }
        } catch (JAXBException e) {
            throw new RuntimeException(e);                                            // JAXBException은 복구 불가능한 예외다. 불필요한 throws를 피하도록 런타임 예외로 포장해서 던진다.
        }
    }
}
