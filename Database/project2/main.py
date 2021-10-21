import shutil
import warnings
import pymysql as sql

class CONDITIONS:
    CAPACITY = ((lambda v: v > 0), 'Capacity should be more than 0')
    GENDER = ((lambda v : v in ('M', 'F')), 'Gender should be \'M\' or \'F\'')
    AGE = ((lambda v: v > 0), 'Age should be more than 0')
    PRICE = ((lambda v: v >= 0), 'Price should be 0 or more')


class DB:
    
    def __init__(self):
        # mysql -h s.snu.ac.kr -u DB2015_16327 -pwhdrkdwhdrkd DB2015_16327

        # db connection variable
        self.conn = sql.connect(
            host='<REDACTED>',
            user='<REDACTED>',
            password='<REDACTED>',
            db='<REDACTED>',
            charset='utf8',
            cursorclass=sql.cursors.DictCursor,
            autocommit=False,
        )

        # actions maps for user selection
        self.actions = {
            1: self.action_print_all_buildings,
            2: self.action_print_all_perfomances,
            3: self.action_print_all_audiences,
            4: self.action_insert_building,
            5: self.action_remove_building,
            6: self.action_insert_performance,
            7: self.action_remove_performance,
            8: self.action_insert_audience,
            9: self.action_remove_audience,
            10: self.action_assign_performance,
            11: self.action_book_performance,
            12: self.action_print_all_performances_assigned,
            13: self.action_print_all_audiences_booked,
            14: self.action_print_ticket,
            15: self.action_exit,
            16: self.action_reset_database,
        }
    

    def show_choices(self):
        print('''============================================================
1. print all buildings
2. print all performances
3. print all audiences
4. insert a new building
5. remove a building
6. insert a new performance
7. remove a performance
8. insert a new audience
9. remove an audience
10. assign a performance to a building
11. book a performance
12. print all performances which assigned at a building
13. print all audiences who booked for a performance
14. print ticket booking status of a performance
15. exit
16. reset database
============================================================''')

    def _assert(self, value, condition):
        """helper function to check value constraint"""
        f = condition[0]
        error = condition[1]
        if not f(value):
            raise ValueError(error)

    def _truncate_key(self, rows, keys):
        """ fetch returns {key: value} pair, get only values and truncate all keys specified in `keys`"""
        _rows = []
        for row in rows:
            _row = []
            for key in keys:
                _row.append(row[key])
            _rows.append(_row)
        return _rows
        
    def _dump_row(self, row, pad):
        """ dump row to generate select output"""
        return map(lambda r: str(r).ljust(pad), row)

    def _dump_table(self, values, columns):
        """show table (result of select) in pretty way
           always fit to terminal size Yay! :)"""
        
        cols = shutil.get_terminal_size()[0] - 1
        bar = '-' * cols
        size_per_column = cols // len(columns)
        print(bar) # --------
        print(''.join(self._dump_row(columns, size_per_column)))
        print(bar) # --------
        for row in values:
            print(''.join(self._dump_row(row, size_per_column)))
        print(bar) # --------
    
    def _calc_price(self, price, age):
        """price calculation accroding to age"""
        if 1 <= age < 8:
            return 0
        elif 8 <= age < 13:
            return price * 0.5
        elif 13 <= age < 19:
            return price * 0.8
        else:
            return price

    def _clear_db(self):
        # name of tables
        # order is IMPORTANT since foreign key contstraint exists!
        tables = ['booking', 'audience', 'performance', 'building']

        with self.conn.cursor() as cursor:
            # drop all tables
            with warnings.catch_warnings(): # dropping table which not exists occurs an error, ignore it
                warnings.simplefilter('ignore')
                for table in tables:
                    cursor.execute('DROP TABLE IF EXISTS %s' % table)
       
    def _check_id(self, table, row_id):
        """ check column with given id exists"""
        query = [
            'SELECT * from %s' % table,
            'WHERE id = %d' % row_id,
        ]

        with self.conn.cursor() as cursor:
            result = cursor.execute(' '.join(query))

        if result == 0: # result will be number of rows
            return False
        
        return True

    # 1
    def action_print_all_buildings(self):
        query = [
            'SELECT building.id, building.name, building.location, building.capacity, T.assigned',
            'FROM',
                'building,',
                '(SELECT building.id, count(performance.building) AS assigned',
                'FROM building LEFT OUTER JOIN performance',
                'ON building.id = performance.building',
                'GROUP BY building.id) AS T',
            'WHERE building.id = T.id'
        ]

        with self.conn.cursor() as cursor:
            cursor.execute(' '.join(query))
            result = cursor.fetchall()
        
        keys = ('id', 'name', 'location', 'capacity', 'assigned')
        result = self._truncate_key(result, keys)
        self._dump_table(result, keys)

        return True

    # 2
    def action_print_all_perfomances(self):
        query = [
            'SELECT performance.id, performance.name, performance.type, performance.price, T.booked',
            'FROM',
                'performance,',
                '(SELECT performance.id, count(distinct booking.audience_id) AS booked',
                'FROM performance LEFT OUTER JOIN booking',
                'ON performance.id = booking.performance_id',
                'GROUP BY performance.id) AS T',
            'WHERE performance.id = T.id',
        ]

        with self.conn.cursor() as cursor:
            cursor.execute(' '.join(query))
            result = cursor.fetchall()
        
        keys = ('id', 'name', 'type', 'price', 'booked')
        result = self._truncate_key(result, keys)
        self._dump_table(result, keys)

        return True

    # 3
    def action_print_all_audiences(self):
        query = [
            'SELECT id, name, gender, age',
            'FROM audience',
        ]

        with self.conn.cursor() as cursor:
            cursor.execute(' '.join(query))
            result = cursor.fetchall()
        
        keys = ('id', 'name', 'gender', 'age')
        result = self._truncate_key(result, keys)
        self._dump_table(result, keys)

        return True

    # 4
    def action_insert_building(self):
        name = input('Building name: ')[:200]
        location = input('Building location: ')[:200]
        capacity = int(input('Building capacity: '))
        self._assert(capacity, CONDITIONS.CAPACITY)

        query = [
            'INSERT INTO building',
            '(name, location, capacity)'
            'VALUES("%s", "%s", %d)' % (name, location, capacity),
        ]

        with self.conn.cursor() as cursor:
            cursor.execute(' '.join(query))
        
        self.conn.commit()
        print('A building is successfully inserted')

        return True

    # 5
    def action_remove_building(self):
        bid = int(input('Building ID: '))
        query = [
            'DELETE FROM building',
            'WHERE id = %d' % bid,
        ]

        with self.conn.cursor() as cursor:
            result = cursor.execute(' '.join(query))
        
        if result == 0: # result will be number or deleted rows
            raise ValueError('Building %d doesn\'t exist' % bid)
        
        self.conn.commit()
        print('A building is successfully removed')
        
        return True

    # 6
    def action_insert_performance(self):
        name = input('Performance name: ')[:200]
        ptype = input('Performance type: ')[:200] # avoiding builtin names
        price = int(input('Performance price: '))
        self._assert(price, CONDITIONS.PRICE)
        
        query = [
            'INSERT INTO performance',
            '(name, type, price, building)',
            # initially assigned building is null
            'VALUES("%s", "%s", %d, %s)' % (name, ptype, price, 'null'),
        ]

        with self.conn.cursor() as cursor:
            cursor.execute(' '.join(query))
        
        self.conn.commit()
        print('A performance is successfully inserted')
        
        return True       

    # 7
    def action_remove_performance(self):
        pid = int(input('Performance ID: '))
        query = [
            'DELETE FROM performance',
            'WHERE id = %d' % pid,
        ]

        with self.conn.cursor() as cursor:
            result = cursor.execute(' '.join(query))
        
        if result == 0: # result will be number of rows
            raise ValueError('Performance %d doesn\'t exist' % pid)
        
        self.conn.commit()
        print('A performance is successfully removed')
        
        return True

    # 8
    def action_insert_audience(self):
        name = input('Audience name: ')[:200]
        gender = input('Audience gender: ')
        self._assert(gender, CONDITIONS.GENDER)
        
        age = int(input('Audience age: '))
        self._assert(age, CONDITIONS.AGE)
        
        query = [
            'INSERT INTO audience',
            '(name, gender, age)',
            # initially assigned building is null
            'VALUES("%s", "%s", %d)' % (name, gender, age),
        ]

        with self.conn.cursor() as cursor:
            cursor.execute(' '.join(query))
        
        self.conn.commit()        
        print('An audience is successfully inserted')

        return True

    # 9
    def action_remove_audience(self):
        aid = int(input('Audience ID: '))
        query = [
            'DELETE FROM audience',
            'WHERE id = %d' % aid,
        ]

        with self.conn.cursor() as cursor:
            result = cursor.execute(' '.join(query))
        
        if result == 0: # result will be number of rows
            raise ValueError('Audience %d doesn\'t exist' % aid)
        
        self.conn.commit()
        print('An audience is successfully removed')
        
        return True

    # 10
    def action_assign_performance(self):
        bid = int(input('Building ID: '))

        # check bulding exists
        if not self._check_id('building', bid):
            raise ValueError('Building %d doesn\'t exist' % bid)

        pid = int(input('Performance ID: '))

        # check performance exists
        if not self._check_id('performance', pid):
            raise ValueError('Performance %d doesn\'t exist' % pid)
        
        query = [
            'UPDATE performance',
            'SET building = %d' % bid,
            # only update when building is null (not already assigned)
            'WHERE id = %d AND building IS NULL' % pid,
        ]

        with self.conn.cursor() as cursor:
            result = cursor.execute(' '.join(query))
        
        if result == 0: # result will be number of rows
            raise ValueError('Performance %d is already assigned' % pid)
        
        self.conn.commit()
        print('Successfully assign a performance')
        return True

    # 11
    def action_book_performance(self):
        pid = int(input('Performance ID: '))

        if not self._check_id('performance', pid):
            raise ValueError('Performance %d doesn\'t exist' % pid)

        aid = int(input('Audience ID: '))

        if not self._check_id('audience', aid):
            raise ValueError('Audience %d doesn\'t exist' % aid)

        seats = input('Seat Number: ')
        seats = list(map(lambda v: int(v.strip()), seats.split(','))) # '1, 2, 3' --> [1, 2, 3]
        
        query = [
            'SELECT price, capacity, building.id FROM building, performance',
            'WHERE performance.id = %d AND building.id = performance.building' % pid
        ]

        with self.conn.cursor() as cursor:
            result = cursor.execute(' '.join(query))

            if result == 0:
                raise ValueError('Performance %d isn\'t assigned' % pid)

            performance = cursor.fetchone()
        
        query = [
            'SELECT * FROM audience',
            'WHERE id = %d' % aid,
        ]

        with self.conn.cursor() as cursor:
            result = cursor.execute(' '.join(query))
            audience = cursor.fetchone()   

        for seat in seats:

            if seat < 1 or seat > performance['capacity']:
                raise ValueError('Seat number out of range')

            query = [
                'SELECT * FROM booking',
                'WHERE performance_id = %d AND seat_no = %d' % (pid, seat),
            ]

            with self.conn.cursor() as cursor:
                result = cursor.execute(' '.join(query))

                if result != 0: # if there is a entry in booking with selected performance and seat
                    raise ValueError('The seat is already taken')
        
        for seat in seats:
            query = [
                'INSERT INTO booking',
                '(performance_id, audience_id, building_id, seat_no)',
                'values(%d, %d, %d, %d)' % (pid, aid, performance['id'], seat),
            ]

            with self.conn.cursor() as cursor:
                result = cursor.execute(' '.join(query))    
        
        total_price = int((self._calc_price(performance['price'], audience['age']) * len(seats)) + 0.5)

        self.conn.commit()
        print('Successfully book a performance')
        print('Total ticket price is %d' % total_price)

        return True

    # 12
    def action_print_all_performances_assigned(self):
        bid = int(input('Building ID: '))
        
        if not self._check_id('building', bid):
            raise ValueError('Building %d doesn\'t exist' % bid)
        
        query = [
            'SELECT performance.id, performance.type, performance.price, T.booked',
            'FROM',
                'performance,',
                '(SELECT performance.id, count(booking.seat_no) AS booked',
                'FROM performance LEFT OUTER JOIN booking',
                'ON performance.id = booking.performance_id',
                'GROUP BY performance.id) AS T',
            'WHERE performance.id = T.id AND performance.building = %d' % bid,
        ]
        
        with self.conn.cursor() as cursor:
            cursor.execute(' '.join(query))
            result = cursor.fetchall()
        
        keys = ('id', 'type', 'price', 'booked')
        result = self._truncate_key(result, keys)
        self._dump_table(result, keys)

        return True

    # 13
    def action_print_all_audiences_booked(self):
        pid = int(input('Performance ID: '))
        if not self._check_id('performance', pid):
            raise ValueError('Performance %d doesn\'t exist' % pid)
        
        query = [
            'SELECT DISTINCT audience.id, audience.name, audience.gender, audience.age',
            'FROM audience JOIN booking',
            'ON audience.id = booking.audience_id',
            'WHERE booking.performance_id = %d' % pid,
        ]

        with self.conn.cursor() as cursor:
            cursor.execute(' '.join(query))
            result = cursor.fetchall()
        
        keys = ('id', 'name', 'gender', 'age')
        result = self._truncate_key(result, keys)
        self._dump_table(result, keys)

        return True

    # 14
    def action_print_ticket(self):
        pid = int(input('Performance ID: '))
        if not self._check_id('performance', pid):
            raise ValueError('Performance %d doesn\'t exist' % pid)
        
        query = [
            'SELECT capacity FROM building, performance',
            'WHERE performance.id = %d AND building.id = performance.building' % pid
        ]

        with self.conn.cursor() as cursor:
            result = cursor.execute(' '.join(query))

            if result == 0:
                raise ValueError('Performance %d isn\'t assigned' % pid)

            performance = cursor.fetchone()

        query = [
            'SELECT audience_id, seat_no FROM booking',
            'WHERE performance_id = %d' % pid,
        ]

        with self.conn.cursor() as cursor:
            cursor.execute(' '.join(query))
            audiences = self._truncate_key(cursor.fetchall(), ('seat_no', 'audience_id'))
        
        # fill seat booking table, if no audience booked, use empty string
        rows = [[i, ''] for i in range(1, performance['capacity'] + 1)]
        for audience in audiences:
            rows[audience[0] - 1][1] = audience[1]
        
        keys = ('seat_number', 'audience_id')
        self._dump_table(rows, keys)

        return True
    
    # 15
    def action_exit(self):
        self.conn.close()
        print('Bye!')
        return False # return False on exit
    
    # 16
    def action_reset_database(self):

        while True:
            chk = input('Reset all Schema [y/n]? ')
            if chk == 'y':
                break
            elif chk == 'n':
                return True
            else:
                print('please input y or n')

        # clear db before creation
        self._clear_db()

        # building

        query = [
            'CREATE TABLE building (',
                'id INT(11) AUTO_INCREMENT,',
                'name VARCHAR(200),',
                'location VARCHAR(200),',
                'capacity INT(11),',
                'PRIMARY KEY (id),',
                'CHECK (capacity > 0)'
            ')',
        ]

        with self.conn.cursor() as cursor:
            cursor.execute(' '.join(query))
        
        # performance

        query = [
            'CREATE TABLE performance (',
                'id INT(11) AUTO_INCREMENT,',
                'name VARCHAR(200),',
                'type VARCHAR(200),',
                'price INT(11),',
                'building INT(11),',
                'PRIMARY KEY (id),',
                'FOREIGN KEY (building) REFERENCES building(id) ON DELETE SET NULL,',
                'CHECK (price >= 0)',
            ')',
        ]

        with self.conn.cursor() as cursor:
            cursor.execute(' '.join(query))

        # audience

        query = [
            'CREATE TABLE audience (',
                'id INT(11) AUTO_INCREMENT,',
                'name VARCHAR(200),',
                'gender CHAR(1),',
                'age INT(11),',
                'PRIMARY KEY (id),',
                'CHECK (age > 0),',
                'CHECK (gender="M" or gender="F")',
            ')',
        ]

        with self.conn.cursor() as cursor:
            cursor.execute(' '.join(query))
        
        # booking (audience-performance)

        query = [
            'CREATE TABLE booking (',
                'audience_id INT(11),',
                'performance_id INT(11),',
                'building_id INT(11),',
                'seat_no INT(11),',
                'PRIMARY KEY (audience_id, performance_id, seat_no),',
                'FOREIGN KEY (audience_id) REFERENCES audience(id) ON DELETE CASCADE,',
                'FOREIGN KEY (performance_id) REFERENCES performance(id) ON DELETE CASCADE,',
                'FOREIGN KEY (building_id) REFERENCES building(id) ON DELETE CASCADE',
            ')',
        ]

        with self.conn.cursor() as cursor:
            cursor.execute(' '.join(query))
             
        # commit altogether
        # if error occurs, nothing is commited
        self.conn.commit()
        return True


    def select_action(self):
        while True:
            try:
                inp = int(input('Select your action: '))
            except ValueError as e: 
                print('Invalid action')

            try:
                # if action returns False, return
                if (not self.actions[inp]()):
                    return
            except KeyError as e:
                print('Invalid action')
            except ValueError as e:
                print(str(e))


def main():
    db = DB()
    db.show_choices()
    db.select_action()

if __name__ == '__main__':
    main()