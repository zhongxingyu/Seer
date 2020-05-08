 package au.net.netstorm.boost.test.lifecycle;
 
public interface TestLifecycle {
    void pre();

    void post();

    void cleanup(boolean successful);
 }
