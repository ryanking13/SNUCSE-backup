/* code adapted from : https://stackoverflow.com/questions/26995166/how-to-get-cartesian-product-from-lists */

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class CartesianProduct {

    public static <T> List<ArrayList<T>> calculate(List<ArrayList<T>> input) {
        List<ArrayList<T>> res = new ArrayList<>();
        if (input.isEmpty()) { // if no more elements to process
            res.add(new ArrayList<>()); // then add empty list and return
            return res;
        } else {
            process(input, res); // we need to calculate the cartesian product of input and store it in res variable
        }
        return res; // method completes , return result
    }

    private static <T> void process(List<ArrayList<T>> lists, List<ArrayList<T>> res) {
        ArrayList<T> head = lists.get(0); //take first element of the list
        List<ArrayList<T>> tail = calculate(lists.subList(1, lists.size())); //invoke calculate on remaining element, here is recursion

        for (T h : head) { // for each head
            for (List<T> t : tail) { //iterate over the tail
                ArrayList<T> tmp = new ArrayList<>(t.size());
                tmp.add(h); // add the head
                tmp.addAll(t); // and current tail element
                res.add(tmp);
            }
        }
    }
}