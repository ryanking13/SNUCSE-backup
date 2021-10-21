# returns f1 score
# consider zero-zero as true-positive
def f1_score(real, predicted):
    zero_match = 0      # both zero
    one_match = 0       # both one
    one_zero = 0        # real: one, predicted: zero
    zero_one = 0        # real: zero, predicted: one

    if len(real) != len(predicted):
        print("Length differs")
        return None

    for i in range(len(real)):
        r = real[i]
        p = predicted[i]
        if r == 0 and p == 0:
            zero_match += 1
        elif r == 1 and p == 1:
            one_match += 1
        elif r == 1 and p == 0:
            one_zero += 1
        else:
            zero_one += 1

    
    if zero_match == 0:
        return 0;
        
    precision = zero_match / (zero_match + one_zero)
    recall = zero_match / (zero_match + zero_one)

    f1 = 2 * (precision * recall) / (precision + recall)

    return f1
