public class Stream4 {
    public static void main(String args[]) {
        List<Integer> numbers = Arrays.asList(1,2,3,4,5,6,7,8,9,10);
        List<Intger> bigs = numbers.stream().filter((Integer i) -> 
        {
            System.out.prinln("filtering: " + i)
            return i > 5;
            }).map((Integer i) -> {
                System.out.println("mapping" + i)
                return i+10
            }).collection(toList();)
    }
}