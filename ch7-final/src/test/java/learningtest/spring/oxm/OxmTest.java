package learningtest.spring.oxm;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import user.config.UserConfig;
import user.sqlservice.jaxb.SqlType;
import user.sqlservice.jaxb.Sqlmap;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = UserConfig.class)
public class OxmTest {

    @Autowired
    Unmarshaller unmarshaller;                                    // 스프링 테스트가 테스트용 애플리케이션 컨텍스트에서 Unmarshaller 인터페이스 타입의 빈을 찾아서 테스트가 시작되기 전에 이 변수에 넣어준다.

    @Test
    public void readSqlmap() throws XmlMappingException, IOException {
        Source xmlSource = new StreamSource(
                getClass().getResourceAsStream("/sqlmap_test.xml")            // InputStream을 이용하는 Source 타입의 StreamSource를 만든다.
        );

        Sqlmap sqlmap = (Sqlmap) this.unmarshaller.unmarshal(xmlSource);    // 어떤 OXM 기술이든 언마샬은 이 한 줄이면 끝이다.

        List<SqlType> sqlList = sqlmap.getSql();

        assertThat(sqlList.size(), is(3));
        assertThat(sqlList.get(0).getKey(), is("add"));
        assertThat(sqlList.get(0).getValue(), is("insert"));
        assertThat(sqlList.get(1).getKey(), is("get"));
        assertThat(sqlList.get(1).getValue(), is("select"));
        assertThat(sqlList.get(2).getKey(), is("delete"));
        assertThat(sqlList.get(2).getValue(), is("delete"));
    }
}
