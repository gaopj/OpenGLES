package gpj.com.camera;

public class Utils {

    public static float[] vector3DCross(float[]v1,float[]v2){
        if(v1.length!=v2.length)
            return null;
        if(v1.length!=3)
            return null;
        return new float[] {v1[1]*v2[2]-v2[1]*v1[2],v1[2]*v2[0]-v2[2]*v1[0],v1[0]*v2[1]-v2[0]*v1[1]};
    }

    public static float[] vectorInversion(float[]v){
        float[] res = new  float[v.length];
        for(int i=0;i<v.length;i++){
            res[i] = -v[i];
        }
        return res;
    }

    public static float[] vectorAdd(float[]v1,float[]v2){
        if(v1.length!=v2.length)
            return null;
        float[] res = new  float[v1.length];
        for(int i=0;i<v1.length;i++){
            res[i] = v1[i]+v2[i];
        }
        return res;
    }

    public static float[] vectorSub(float[]v1,float[]v2){
        return vectorAdd(v1,vectorInversion(v2));
    }

    public static float[] vectorMul(float[]v,float s){
        float[] res = new  float[v.length];
        for(int i=0;i<v.length;i++){
            res[i] = v[i]*s;
        }
        return res;
    }
}
