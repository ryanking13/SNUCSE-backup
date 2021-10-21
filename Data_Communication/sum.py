f = open('stat.csv', 'rt')

lines = f.readlines()

a = 0
b = 0
c = 0

for line in lines:
    line = line.split()

    a += float(line[0])
    b += float(line[1])
    c += float(line[2])

a /= len(lines)
b /= len(lines)
c /= len(lines)

print(len(lines))
print(a, b, c)
